import { beforeEach, describe, expect, it, Mock, vi } from "vitest";
import { api } from "../../src/config/api";

vi.mock("../../src/config/api", () => ({
  api: {
    post: vi.fn(),
    put: vi.fn(),
    get: vi.fn(),
    delete: vi.fn(),
  },
}));

const mockRefetch = vi.fn();

describe("basket", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(console, "error").mockImplementation(() => {});
  });

  describe("changeQuantity", () => {
    const changeQuantity = async (
      productItemId: number,
      operation: string,
      jwt: string,
      refetch: () => void
    ) => {
      try {
        await api.put(
          `/api/v1/cart/update/products/${productItemId}/quantity/${operation}`,
          {},
          {
            headers: {
              Authorization: `Bearer ${jwt}`,
            },
          }
        );
        refetch();
      } catch (error) {
        console.error(error);
      }
    };

    it("should update quantity", async () => {
      const jwt = "fake-jwt-token";
      const productItemId = 123;
      const operation = "decrease";

      await changeQuantity(productItemId, operation, jwt, mockRefetch);

      expect(api.put).toHaveBeenCalledWith(
        `/api/v1/cart/update/products/${productItemId}/quantity/${operation}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${jwt}`,
          },
        }
      );

      expect(mockRefetch).toHaveBeenCalled();
    });

    it("should handle error when changing quantity fails", async () => {
      (api.put as Mock).mockRejectedValue(new Error("API Error"));

      await changeQuantity(123, "increase", "jwt", mockRefetch);

      expect(console.error).toHaveBeenCalled();
      expect(mockRefetch).not.toHaveBeenCalled();
    });
  });

  describe("deleteProductItemFromCart", () => {
    const deleteProductItemFromCart = async (
      productItemId: number,
      cartId: number,
      refetch: () => void
    ) => {
      try {
        if (!cartId) return;

        await api.delete(
          `/api/v1/cart/delete/${cartId}/product/${productItemId}`
        );
        refetch();
      } catch (error) {
        console.error(error);
      }
    };

    it("should delete product from cart", async () => {
      const cartId = 111;
      const productItemId = 123;

      await deleteProductItemFromCart(productItemId, cartId, mockRefetch);

      expect(api.delete).toHaveBeenCalledWith(
        `/api/v1/cart/delete/${cartId}/product/${productItemId}`
      );
      expect(mockRefetch).toHaveBeenCalled();
    });

    it("should not call API when cart is not available", async () => {
      await deleteProductItemFromCart(123, undefined, mockRefetch);

      expect(api.delete).not.toHaveBeenCalled();
      expect(mockRefetch).not.toHaveBeenCalled();
    });
  });

  describe("addDiscountCode", () => {
    const discountCode = "DISCOUNT";

    const addDiscountCode = async (
      cartId: number,
      discountCode: string,
      refetch: () => void
    ) => {
      if (!discountCode.trim()) return;

      try {
        await api.post(
          `/api/v1/cart/add-discount/${cartId}?discountCode=${discountCode}`
        );
        refetch();
      } catch (error) {
        console.log(error);
      }
    };

    it("should add discount code", async () => {
      const cartId = 123;
      await addDiscountCode(cartId, discountCode, mockRefetch);

      expect(api.post).toHaveBeenCalledWith(
        `/api/v1/cart/add-discount/${cartId}?discountCode=${discountCode}`
      );
      expect(mockRefetch).toHaveBeenCalled();
    });

    it("should not call API with empty discount code", async () => {
      await addDiscountCode(123, "", mockRefetch);

      expect(api.post).not.toHaveBeenCalled();
    });
  });

  describe("submitOrder", () => {
    const submitOrder = async ({
      cart,
      userId,
      selectedAddress,
      selectedShippingMethod,
      totalPrice,
      totalDiscount,
      calculateTotalCartValue,
      formikValues,
      api,
      windowObj = window,
    }: {
      cart: any;
      userId: string;
      selectedAddress: any;
      selectedShippingMethod: any;
      totalPrice: () => number;
      totalDiscount: number;
      calculateTotalCartValue: () => number;
      formikValues: any;
      api: any;
      windowObj?: Window;
    }) => {
      if (!cart) return;

      const orderRequest = {
        userId,
        orderDate: "",
        addressRequest: {
          id: selectedAddress?.id,
          country: formikValues.country,
          city: formikValues.city,
          firstName: formikValues.firstName,
          lastName: formikValues.lastName,
          postalCode: formikValues.postalCode,
          street: formikValues.street,
          phoneNumber: formikValues.phoneNumber,
          addressLine1: formikValues.addressLine1,
          addressLine2: formikValues.addressLine2,
        },
        cartId: cart.id,
        shippingMethodId: selectedShippingMethod?.id ?? 0,
        orderTotal: Number(totalPrice()),
        finalOrderTotal:
          Number(calculateTotalCartValue()) +
          (selectedShippingMethod?.price ?? 0),
        appliedDiscountValue: totalDiscount ?? 0,
      };

      const request = {
        orderRequest,
        successUrl: `${windowObj.location.origin}/payment-success`,
        cancelUrl: `${windowObj.location.origin}/payment-cancel`,
      };

      try {
        const response = await api.post(
          `/api/v1/payment/stripe/checkout`,
          request
        );
        windowObj.location.href = response.data.checkoutUrl;
      } catch (error) {
        console.error("Error creating order: ", error);
      }
    };

    it("should send the order request", async () => {
      const api = {
        post: vi.fn().mockResolvedValue({
          data: {
            checkoutUrl: "https://stripe.com/checkout/123",
          },
        }),
      };

      const windowMock = {
        location: {
          origin: "http://localhost:5173",
          href: "",
        },
      } as unknown as Window;

      const mockCart = { id: 1 };
      const mockUserId = "user-123";
      const mockAddress = { id: 10 };
      const mockShipping = { id: 5, price: 15 };
      const formikValues = {
        country: "PL",
        city: "Warszawa",
        firstName: "Jan",
        lastName: "Kowalski",
        postalCode: "00-001",
        street: "ul. Testowa",
        phoneNumber: "123456789",
        addressLine1: "adres 1",
        addressLine2: "adres 2",
      };

      await submitOrder({
        cart: mockCart,
        userId: mockUserId,
        selectedAddress: mockAddress,
        selectedShippingMethod: mockShipping,
        totalPrice: () => 100,
        calculateTotalCartValue: () => 80,
        totalDiscount: 20,
        formikValues,
        api,
        windowObj: windowMock,
      });

      expect(api.post).toHaveBeenCalledWith(
        "/api/v1/payment/stripe/checkout",
        expect.objectContaining({
          orderRequest: expect.objectContaining({
            userId: "user-123",
            shippingMethodId: 5,
            finalOrderTotal: 95,
          }),
          successUrl: "http://localhost:5173/payment-success",
          cancelUrl: "http://localhost:5173/payment-cancel",
        })
      );

      expect(windowMock.location.href).toBe("https://stripe.com/checkout/123");
    });

    it("should not call api", async () => {
      const api = {
        post: vi.fn(),
      };

      await submitOrder({
        cart: null,
        userId: "123",
        selectedAddress: null,
        selectedShippingMethod: null,
        totalPrice: () => 0,
        calculateTotalCartValue: () => 0,
        totalDiscount: 0,
        formikValues: {},
        api,
      });

      expect(api.post).not.toHaveBeenCalled();
    });
  });
});
