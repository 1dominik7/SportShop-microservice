import { useCartByUserId } from "../../hooks/query";
import CloseIcon from "@mui/icons-material/Close";
import { ShippingMethod, ShoppingCartItem } from "../../types/userTypes";
import { api } from "../../config/api";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import { useNavigate } from "react-router";
import { useEffect, useState } from "react";
import ImageLoader from "../../ui/ImageLoader";

const CartSlider = ({
  isOpen,
  onClose,
}: {
  isOpen: boolean;
  onClose: () => void;
}) => {
  const navigate = useNavigate();

  const [shippingMethods, setShippingMethods] = useState<
    ShippingMethod[] | null
  >(null);

  const { data: cart, refetch } = useCartByUserId();
  const shoppingCartItems: ShoppingCartItem[] = cart?.shoppingCartItems || [];

  const deleteItemFromCart = async (productItemId: number) => {
    try {
      await api.delete(`/api/v1/cart/delete/product/${productItemId}`);
      refetch();
    } catch (error: any) {
      console.error(error);
    }
  };

  const getProductPrice = (cartItem: ShoppingCartItem) => {
    const selectedVariant = cartItem?.productItem?.productItemOneByColour?.find(
      (variant) => variant.id === cartItem?.productItem?.productItemId
    );

    if (!selectedVariant) return 0;

    if (selectedVariant.discount > 0) {
      return (
        selectedVariant.price -
        selectedVariant.price * (selectedVariant.discount / 100)
      );
    }
    return selectedVariant.price || 0;
  };

  const calculateTotalCartValue = () => {
    const totalWithoutDiscounts = shoppingCartItems.reduce(
      (total, cartItem) => {
        const selectedVariant =
          cartItem?.productItem?.productItemOneByColour?.find(
            (variant) => variant.id === cartItem?.productItem?.productItemId
          );

        if (!selectedVariant) return total;

        const price = selectedVariant.price;
        const discount = selectedVariant.discount;
        const quantity = cartItem?.qty || 0;

        const discountedPrice =
          discount > 0 ? price - price * (discount / 100) : price;

        return total + discountedPrice * quantity;
      },
      0
    );

    const totalDiscount =
      cart?.discountCodes?.reduce((total, code) => {
        const codeDiscount = code.discount;
        return total + totalWithoutDiscounts * (codeDiscount / 100);
      }, 0) || 0;

    return (totalWithoutDiscounts - totalDiscount).toFixed(2);
  };

  useEffect(() => {
    const getShippingMethod = async () => {
      try {
        const res = await api.get(`/api/v1/shipping-method/all`);
        setShippingMethods(res.data);
      } catch (error) {
        console.error("Error during fetching shipping method", error);
      }
    };
    getShippingMethod();
  }, []);

  const getTheLowestShippingPrice = () => {
    if (!shippingMethods || shippingMethods.length === 0) return null;

    const lowest = shippingMethods.reduce((min, method) => {
      return method?.price < min?.price ? method : min;
    });

    return lowest?.price;
  };

  return (
    <div
      className={`fixed top-0 right-0 w-full h-full z-50 transition-opacity delay-100 duration-200 ease-in-out ${
        isOpen
          ? "opacity-100 pointer-events-auto"
          : "opacity-0 pointer-events-none"
      }`}
    >
      <div
        className="fixed top-0 left-0 w-full h-full bg-white opacity-40 z-40"
        onClick={onClose}
      ></div>
      <div
        className={`fixed p-6 top-0 right-0 w-[450px] h-full bg-white z-50 delay-300 transform transition-transform duration-300 ease-in-out ${
          isOpen
            ? "translate-x-0 max-md:w-[90%]"
            : "translate-x-[400px] max-md:w-[200px] max-md:translate-x-[200px]"
        }`}
      >
        <div className="flex items-center justify-between border-b-[1px] border-gray-300 pb-6">
          <div className="flex gap-2 items-center">
            <span className="text-xl font-semibold">My Basket</span>
            {shoppingCartItems?.length > 0 && (
              <div className="w-5 h-5 bg-secondary-color text-center rounded-full flex items-center justify-center">
                <span className="text-white text-sm">
                  {shoppingCartItems?.length}
                </span>
              </div>
            )}
          </div>
          <CloseIcon
            className="cursor-pointer text-gray-500"
            onClick={onClose}
          />
        </div>
        {shoppingCartItems?.length > 0 ? (
          <div className="flex flex-col gap-4">
            {shoppingCartItems?.map((cart, index) => {
              const selectedVariant =
                cart?.productItem?.productItemOneByColour?.find(
                  (variant) => variant.id === cart?.productItem?.productItemId
                );
              const sizeVariation = selectedVariant?.variations?.find(
                (v) => v.name === "size"
              );
              return (
                <div
                  className="w-full flex gap-4 p-4 border-b-[1px] border-gray-300 cursor-pointer"
                  key={index}
                  onClick={() => {
                    const colourVariation =
                      cart?.productItem?.productItemOneByColour[0]?.variations?.find(
                        (v) => v.name === "colour"
                      );
                    const colourValue = colourVariation?.options?.[0]?.value;

                    navigate(
                      `/products/${cart?.productItem.productId}${
                        colourValue && colourValue !== "Unknown"
                          ? "-" + colourValue.toLowerCase().replace(/\s+/g, "-")
                          : ""
                      }`
                    );

                    onClose();
                  }}
                >
                  <ImageLoader
                    className="h-[150px] max-w-[160px] object-cover"
                    src={cart?.productItem?.productImages[0]?.imageFilename}
                    alt={`${cart?.productItem?.productName} + ${cart?.productItem?.productItemOneByColour[0]?.id}`}
                  />
                  <div className="w-full flex flex-col justify-between">
                    <div className="flex justify-between">
                      <span>{cart?.productItem?.productName}</span>
                      <CloseIcon
                        style={{ fontSize: 20 }}
                        className="cursor-pointer text-gray-500"
                        onClick={(e) => {
                          e.stopPropagation();
                          if (selectedVariant?.id !== undefined) {
                            deleteItemFromCart(selectedVariant.id);
                          } else {
                            console.warn(
                              "There is no selectedVariant.id – you can't delete this item from cart."
                            );
                          }
                        }}
                      />
                    </div>
                    <div className="flex flex-col gap-[1px] text-sm">
                      {sizeVariation?.options?.[0]?.value && (
                        <span>
                          Size: <b>{sizeVariation.options[0].value.toUpperCase()}</b>
                        </span>
                      )}
                      <span>
                        Quantity: <b>{cart?.qty}</b>
                      </span>
                    </div>
                    <span className="font-bold">
                      {getProductPrice(cart).toFixed(2)} $
                    </span>
                  </div>
                </div>
              );
            })}
            <div className="flex items-center justify-between text-lg max-md:text-base">
              <span>Delivery:</span>
              <div className="flex gap-2 items-center">
                <LocalShippingIcon />
                <span>from {getTheLowestShippingPrice()} $</span>
              </div>
            </div>
            {cart?.discountCodes && cart?.discountCodes?.length > 0 && (
              <div className="flex flex-col gap-4">
                {cart?.discountCodes?.map((code) => (
                  <div
                    key={code?.id}
                    className="flex items-center justify-between text-lg max-md:text-base"
                  >
                    <span>Discount: {code?.code}</span>
                    <span className="text-red-300 font-bold">
                      -{code?.discount}%
                    </span>
                  </div>
                ))}
              </div>
            )}
            <div className="flex items-center justify-between text-lg font-bold max-md:text-base">
              <span>Total cost:</span>
              <span>{calculateTotalCartValue()} $</span>
            </div>
            <button
              className="h-[55px] w-full flex bg-black text-white rounded-3xl text-center items-center justify-center font-bold cursor-pointer max-md:h-[45px]"
              onClick={() => {
                onClose();
                navigate(`/basket`);
              }}
            >
              Proceed to checkout
            </button>
          </div>
        ) : (
          <div className="mt-6 text-center">
            <span className="text-gray-500">Basket is empty</span>
          </div>
        )}
      </div>
    </div>
  );
};

export default CartSlider;
