import React, { act } from "react";
import { afterEach, beforeEach, describe, expect, it, vi, Mock } from "vitest";
import { api } from "../../src/config/api";
import {
  Address,
  CartItem,
  Category,
  FamousShoes,
  Filter,
  GetShopOrder,
  Product,
  ProductItem,
  ProductItemByColour,
  ProductItemList,
  ProductItemListGroupedByFilters,
  ProductItemsFilters,
} from "../../src/types/userTypes";
import {
  buildVariationIds,
  buildVariationOptionIds,
  fetchCartByUserId,
  fetchCategoriesById,
  fetchFamousShoesCollection,
  fetchFilters,
  fetchNewestProducts,
  fetchProductById,
  fetchProductItemById,
  fetchProductItemsFilters,
  fetchProductsByFilters,
  fetchProductsByFiltersGrouped,
  fetchUserAddresses,
  fetchUserShopOrders,
  getFavoritesFromCookies,
  setFavoritesInCookies,
  useCartByUserId,
  useCategoryQuery,
  useFamousShoesCollectionQuery,
  useFavorites,
  useFetchFilters,
  useNewestProductsQuery,
  useProductById,
  useProductItemById,
  useProductItemsFilters,
  useProductsByFilters,
  useProductsByFiltersGrouped,
  useToggleFavorite,
  useUserAddresses,
  useUserShopOrders,
} from "../../src/hooks/query";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";

vi.mock("../../src/config/api", () => ({
  api: {
    get: vi.fn(),
  },
}));

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

const mockCategoryData: Category[] = [
  {
    id: 1,
    categoryName: "category 1",
    parentCategoryId: null,
    variations: [],
  },
];

const mockProductItemData: ProductItem[] = [
  {
    id: 1,
    price: 100,
    discount: 0,
    productCode: "ew2",
    productName: "test product",
    qtyInStock: 2,
    variations: [],
    variationOptionIds: [],
    variationOptions: [],
    productImages: [],
    productDescription: "test test",
    categoryId: 1,
    productId: 1,
    otherProductItems: [],
    size: "39",
  },
];

const mockFamousShoesData: FamousShoes[] = [
  {
    totalElements: 1,
  },
];

const mockFiltersnData: Filter[] = [
  {
    categoryId: 1,
    id: 1,
    name: "purpose",
    options: [
      {
        id: 1,
        value: "grass",
      },
      {
        id: 2,
        value: "lifestyle",
      },
    ],
  },
];

const mockProductItemList: ProductItemList = {
  content: [
    {
      id: 1,
      price: 100,
      discount: 0,
      productCode: "ew2",
      productName: "test product",
      qtyInStock: 2,
      variations: [],
      variationOptionIds: [],
      variationOptions: [],
      productImages: [],
      productDescription: "test test",
      categoryId: 1,
      productId: 1,
      otherProductItems: [],
      size: "39",
    },
  ],
  pageNumber: 1,
  pageSize: 1,
  totalElements: 1,
  totalPages: 1,
  lastPage: false,
};

const mockProductItemByColour: ProductItemByColour = {
  id:1,
  productId: 1,
  productItemId:1,
  productName: "test test",
  colour: "blue",
  productImages: [],
  productItemOneByColour: [
    {
      categoryId: 1,
      productId: 1,
      colour: "blue",
      discount: 0,
      id: 1,
      price: 100,
      productCode: "ew22",
      productDescription: "test test desc",
      productImages: [],
      productName: "test name",
      qtyInStock: 2,
      variations: [],
    },
  ],
  otherProductItemOneByColours: [],
};

const mockProductItemGroupedByFilters: ProductItemListGroupedByFilters = {
  content: [
    {
      productId: 1,
      productName: "test name",
      productImages: [],
      variations: [
        {
          id: 1,
          categoryId: null,
          name: "purpose",
          options: [
            {
              id: 1,
              value: "grass",
            },
            {
              id: 2,
              value: "artificial grass",
            },
          ],
        },
      ],
      productItemRequests: [],
      colour: "blue",
      size: "40",
    },
  ],
  pageNumber: 1,
  pageSize: 1,
  totalElements: 1,
  totalPages: 1,
  lastPage: false,
};

const mockProductItemsFilters: ProductItemsFilters[] = [
  {
    categoryId: 1,
    variation: {
      id: 1,
      categoryId: null,
      name: "purpose",
      options: [
        {
          id: 1,
          value: "grass",
        },
        {
          id: 2,
          value: "artificial grass",
        },
      ],
    },
  },
];

const mockProduct: Product = {
  id: 1,
  productName: "test name",
  description: "test desc",
  categoryId: null,
  productItems: [
    {
      id: 1,
      price: 100,
      discount: 0,
      productCode: "ew2",
      productName: "test product",
      qtyInStock: 2,
      variations: [],
      variationOptionIds: [],
      variationOptions: [],
      productImages: [],
      productDescription: "test test",
      categoryId: 1,
      productId: 1,
      otherProductItems: [],
      size: "39",
    },
  ],
};

const mockCartItem: CartItem = {
  id: 1,
  shoppingCartItems: [
    {
      id: 1,
      productItem: {
        id: 1,
        productId: 1,
        productItemId: 1,
        productName: "Test Product",
        colour: "black",
        productItemOneByColour: [],
        otherProductItemOneByColours: [],
        productImages: [],
      },
      qty: 1,
    },
  ],
};

const mockAddress: Address[] = [
  {
    id: 1,
    country: "Poland",
    city: "city",
    firstName: "firstname",
    lastName: "lastname",
    postalCode: "33-333",
    street: "street",
    phoneNumber: "777 777 777",
    addressLine1: "address1",
    addressLine2: "address2",
  },
];

const mockGetShopOrder: GetShopOrder[] = [
  {
    id: 1,
    userId: "1",
    appliedDiscountValue: 0,
    finalOrderTotal: 120,
    orderDate: "22.12.2024",
    orderLines: [],
    orderStatus: { id: 1, status: "inProgress" },
    orderTotal: 1,
    paymentCreatedAt: "22.12.2024",
    paymentId: 1,
    paymentIntentId: 0,
    paymentMethodName: "Stripe",
    paymentStatus: "",
    paymentTransactionId: "22111",
    shippingCity: "city",
    shippingCountry: "Poland",
    shippingFirstName: "firstname",
    shippingLastName: "lastname",
    shippingMethod: {
      id: 1,
      name: "inPost",
      price: 12.22,
    },
    shippingPhoneNumber: "777 777 777",
    shippingPostalCode: "33-333",
    shippingStreet: "street",
    shippingAddressLine1: "address1",
    shippingAddressLine2: "address2",
  },
];

describe("hooks", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.clearAllMocks();
    document.cookie = "";
  });

  describe("fetchCategoriesById", () => {
    it("should fetch categories by id", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockCategoryData,
      });

      const result = await fetchCategoriesById([1]);
      expect(api.get).toHaveBeenCalledWith("/api/v1/category/byId?ids=1");
      expect(result).toEqual(mockCategoryData);
    });

    it("should handle multiple ids", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockCategoryData,
      });

      await fetchCategoriesById([1, 2, 3]);
      expect(api.get).toHaveBeenCalledWith("/api/v1/category/byId?ids=1,2,3");
    });
  });

  describe("useCategoryQuery", () => {
    let consoleSpy: ReturnType<typeof vi.spyOn>;

    beforeEach(() => {
      consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    });

    afterEach(() => {
      consoleSpy.mockRestore();
    });

    it("should fetch categories data by id", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockCategoryData,
      });

      const { result } = renderHook(() => useCategoryQuery([1]), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
        expect(result.current.data).toEqual(mockCategoryData);
      });
    });

    it("should handle errors", async () => {
      const error = new Error("Network error");
      (api.get as Mock).mockRejectedValue(error);

      const { result } = renderHook(() => useCategoryQuery([1]), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isError).toBe(true);
      });
      expect(result.current.error).toEqual(error);
    });
  });

  describe("fetchNewestProducts", () => {
    it("should fetch newest products", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockProductItemData,
      });

      const result = await fetchNewestProducts(1);
      expect(api.get).toHaveBeenCalledWith(
        `/api/v1/productItems/getAll?items=${1}`
      );
      expect(result).toEqual(mockProductItemData);
    });

    it("should handle empty response", async () => {
      (api.get as Mock).mockResolvedValue({
        data: [],
      });

      const result = await fetchNewestProducts(0);

      expect(result).toEqual([]);
    });

    it("should throw error when API fails", async () => {
      (api.get as Mock).mockRejectedValue(new Error("Network error"));

      await expect(fetchNewestProducts(1)).rejects.toThrow("Network error");
    });
  });

  describe("useNewestProductsQuery", () => {
    let consoleSpy: ReturnType<typeof vi.spyOn>;

    beforeEach(() => {
      consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    });

    afterEach(() => {
      consoleSpy.mockRestore();
    });

    it("should fetch newest products data", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockProductItemData,
      });

      const { result } = renderHook(() => useNewestProductsQuery(1), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
        expect(result.current.data).toEqual(mockProductItemData);
      });
    });

    it("should log errors to console", async () => {
      const error = new Error("Network error");
      (api.get as Mock).mockRejectedValue(error);

      renderHook(() => useNewestProductsQuery(1), {
        wrapper: createWrapper(),
      });

      await waitFor(
        () => {
          expect(consoleSpy).toHaveBeenCalledWith(
            "Error fetching newest products: ",
            error
          );
        },
        { timeout: 5000 }
      );
    });
  });

  describe("fetchFamousShoesCollection", () => {
    it("should fetch famous shoes collection", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockFamousShoesData,
      });

      const result = await fetchFamousShoesCollection(1, 1);
      expect(api.get).toHaveBeenCalledWith(
        `/api/v1/products/searchByCategory?variationId=${1}&variationOptionId=${1}&pageNumber=0&pageSize=10&sortBy=id&sortOrder=asc`
      );
      expect(result).toEqual(mockFamousShoesData);
    });
  });

  describe("useFamousShoesCollectionQuery", () => {
    it("should fetch famous shoes collection data", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockFamousShoesData,
      });

      const { result } = renderHook(() => useFamousShoesCollectionQuery(1, 1), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
        expect(result.current.data).toEqual(mockFamousShoesData);
      });
    });
  });

  describe("fetchFilters", () => {
    it("should fetch filters", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockFiltersnData,
      });
      const categoryId = 1;

      const result = await fetchFilters(1);
      expect(api.get).toHaveBeenCalledWith(
        `/api/v1/category/byId?ids=${categoryId}`
      );
      expect(result).toEqual(mockFiltersnData);
    });
  });

  describe("useFetchFilters", () => {
    it("should fetch filters data", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockFiltersnData,
      });

      const { result } = renderHook(() => useFetchFilters(1), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
        expect(result.current.data).toEqual(mockFiltersnData);
      });
    });
  });

  describe("buildVariationIds", () => {
    it("should build variation", () => {
      const input = {
        1: { optionId: [10] },
        2: { optionId: [11, 20] },
      };

      const result = buildVariationIds(input);
      expect(result).toBe("1,2");
    });
  });

  describe("buildVariationOptionIds", () => {
    it("should build variation option", () => {
      const input = {
        1: { optionId: [10] },
        2: { optionId: [20, 21] },
      };

      const result = buildVariationOptionIds(input);
      expect(result).toBe("10,20,21");
    });
  });

  describe("fetchProductsByFilters", () => {
    it("should fetch products by filters", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockProductItemList,
      });
      const categoryId = 1;

      const result = await fetchProductsByFilters(
        1,
        { 1: { optionId: [1] } },
        1,
        1,
        "id",
        "asc"
      );
      expect(api.get).toHaveBeenCalledWith(
        `/api/v1/productItems/searchByCategory?categoryId=${categoryId}&variationIds=1&variationOptionIds=1&pageNumber=1&pageSize=1&sortBy=id&sortOrder=asc`
      );
      expect(result).toEqual(mockProductItemList);
    });
  });

  describe("useProductsByFilters", () => {
    it("should fetch products by filters data", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockProductItemList,
      });

      const { result } = renderHook(
        () => useProductsByFilters(1, { 1: { optionId: [1] } }),
        {
          wrapper: createWrapper(),
        }
      );

      await act(() => result.current.refetch());

      await waitFor(() => {
        expect(result.current.data).toEqual(mockProductItemList);
      });
    });
  });

  describe("fetchProductItemById", () => {
    it("should fetch product item by id", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockProductItemByColour,
      });

      const result = await fetchProductItemById(1, "blue");
      expect(api.get).toHaveBeenCalledWith(
        `/api/v1/productItems/1?colour=blue`
      );
      expect(result).toEqual(mockProductItemByColour);
    });
  });

  describe("useProductItemById", () => {
    it("should fetch product item by id data", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockProductItemByColour,
      });

      const { result } = renderHook(() => useProductItemById(1, "blue"), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
        expect(result.current.data).toEqual(mockProductItemByColour);
      });
    });
  });

  describe("fetchProductsByFiltersGrouped", () => {
    it("should fetch products by filters group", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockProductItemGroupedByFilters,
      });

      const result = await fetchProductsByFiltersGrouped(
        1,
        { 1: { optionId: [1] } },
        1,
        1,
        "id",
        "asc"
      );
      expect(api.get).toHaveBeenCalledWith(
        `/api/v1/productItems/searchByColour?pageNumber=1&pageSize=1&sortBy=id&sortOrder=asc&categoryId=1&variationIds=1&variationOptionIds=1`
      );
      expect(result).toEqual(mockProductItemGroupedByFilters);
    });
  });

  describe("useProductsByFiltersGrouped", () => {
    it("should fetch products by filters group data", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockProductItemGroupedByFilters,
      });

      const { result } = renderHook(
        () =>
          useProductsByFiltersGrouped(
            1,
            { 1: { optionId: [1] } },
            1,
            1,
            "id",
            "asc"
          ),
        {
          wrapper: createWrapper(),
        }
      );

      await act(() => result.current.refetch());

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
        expect(result.current.data).toEqual(mockProductItemGroupedByFilters);
      });
    });
  });

  describe("fetchProductItemsFilters", () => {
    it("should fetch product items filters", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockProductItemsFilters,
      });

      const result = await fetchProductItemsFilters(1, {
        1: { optionId: [1] },
      });
      expect(api.get).toHaveBeenCalledWith(
        `/api/v1/productItems/filters?categoryId=1&variationIds=1&variationOptionIds=1`
      );
      expect(result).toEqual(mockProductItemsFilters);
    });
  });

  describe("useProductItemsFilters", () => {
    it("should fetch product items filters data", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockProductItemsFilters,
      });

      const { result } = renderHook(
        () => useProductItemsFilters(1, { 1: { optionId: [1] } }),
        {
          wrapper: createWrapper(),
        }
      );

      await act(() => result.current.refetch());

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
        expect(result.current.data).toEqual(mockProductItemsFilters);
      });
    });
  });

  describe("fetchProductById", () => {
    it("should fetch product by id", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockProduct,
      });

      const result = await fetchProductById(1);
      expect(api.get).toHaveBeenCalledWith(`/api/v1/products/1`);
      expect(result).toEqual(mockProduct);
    });
  });

  describe("useProductById", () => {
    it("should fetch product by id data", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockProduct,
      });

      const { result } = renderHook(() => useProductById(1), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
        expect(result.current.data).toEqual(mockProduct);
      });
    });
  });

  describe("fetchCartByUserId", () => {
    it("should fetch cart by user id when JWT is provided", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockCartItem,
      });

      const result = await fetchCartByUserId();

      expect(api.get).toHaveBeenCalledWith(`/api/v1/cart/users/cart`);
      expect(result).toEqual(mockCartItem);
    });
  });

  describe("useCartByUserId", () => {
    it("should not fetch when JWT is null", async () => {
      const { result } = renderHook(() => useCartByUserId(), {
        wrapper: createWrapper(),
      });

      expect(result.current.isLoading).toBe(false);
      expect(result.current.isFetching).toBe(false);
      expect(api.get).not.toHaveBeenCalled();
    });

    it("should fetch cart by user id data", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockCartItem,
      });

      const { result } = renderHook(() => useCartByUserId(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
        expect(result.current.data).toEqual(mockCartItem);
      });
    });
  });

  describe("fetchUserAddresses", () => {
    it("should fetch user address", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockAddress,
      });

      const result = await fetchUserAddresses();

      expect(api.get).toHaveBeenCalledWith(`/api/v1/address`);
      expect(result).toEqual(mockAddress);
    });
  });

  describe("useUserAddresses", () => {
    it("should fetch user address data", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockAddress,
      });

      const { result } = renderHook(() => useUserAddresses(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
        expect(result.current.data).toEqual(mockAddress);
      });
    });
  });

  describe("fetchUserShopOrders", () => {
    it("should fetch user shop orders", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockGetShopOrder,
      });

      const result = await fetchUserShopOrders();

      expect(api.get).toHaveBeenCalledWith(`/api/v1/shop-order/user`);
      expect(result).toEqual(mockGetShopOrder);
    });
  });

  describe("useUserShopOrders", () => {
    it("should fetch user shop orders data when JWT is provided", async () => {
      (api.get as Mock).mockResolvedValue({
        data: mockGetShopOrder,
      });

      const { result } = renderHook(() => useUserShopOrders(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
        expect(result.current.data).toEqual(mockGetShopOrder);
      });
    });
  });

  describe("getFavoritesFromCookies", () => {
    it("should return empty array when no favorites cookie exists", () => {
      document.cookie = "";
      const result = getFavoritesFromCookies();
      expect(result).toEqual([]);
    });

    it("should return array with favorites cookie", () => {
      document.cookie = "favorites=[1,2,3]";
      expect(getFavoritesFromCookies()).toEqual([1, 2, 3]);
    });
  });

  describe("setFavoritesInCookies", () => {
    it("should set cookie with correct format", () => {
      const spy = vi.spyOn(document, "cookie", "set");
      setFavoritesInCookies([1, 2, 3]);
      expect(document.cookie).toContain("favorites=[1,2,3]");
      expect(spy).toHaveBeenCalledWith(expect.stringContaining("path=/"));
      spy.mockRestore();
    });
  });

  describe("useFavorites", () => {
    it("should return favorites from cookies", async () => {
      document.cookie = "favorites=[4,5,6]";

      const { result } = renderHook(() => useFavorites(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => expect(result.current.isSuccess).toBe(true));
      expect(result.current.data).toEqual([4, 5, 6]);
    });
  });

  describe("useToggleFavorite", () => {
    it("should add favorite when not present", async () => {
      document.cookie = "favorites=[1,2]";
      const { result } = renderHook(() => useToggleFavorite(), {
        wrapper: createWrapper(),
      });

      await act(async () => {
        await result.current.mutateAsync(3);
      });

      expect(document.cookie).toContain("[1,2,3]");
    });

    it("should return favorites from cookies", async () => {
      document.cookie = "favorites=[1,2,3]";

      const { result } = renderHook(() => useToggleFavorite(), {
        wrapper: createWrapper(),
      });

      await act(async () => {
        await result.current.mutateAsync(2);
      });

      expect(document.cookie).toContain("favorites=[1,3]");
    });

    it("should invalidate favorites query", async () => {
      document.cookie = "favorites=[1]";
      const queryClient = new QueryClient();

      const invalidateSpy = vi.spyOn(queryClient, "invalidateQueries");

      const createWrapper = (client: QueryClient) => {
        return ({ children }: { children: React.ReactNode }) => (
          <QueryClientProvider client={client}>{children}</QueryClientProvider>
        );
      };

      const { result } = renderHook(() => useToggleFavorite(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        await result.current.mutateAsync(2);
      });

      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ["favorites"] });
    });
  });
});
