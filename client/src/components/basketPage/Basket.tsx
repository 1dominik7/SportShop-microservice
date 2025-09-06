import { useEffect, useState } from "react";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import { useNavigate } from "react-router";
import { useCartByUserId } from "../../hooks/query";
import DeleteForeverIcon from "@mui/icons-material/DeleteForever";
import KeyboardArrowUpOutlinedIcon from "@mui/icons-material/KeyboardArrowUpOutlined";
import KeyboardArrowDownOutlinedIcon from "@mui/icons-material/KeyboardArrowDownOutlined";
import WestOutlinedIcon from "@mui/icons-material/WestOutlined";
import LocalOfferOutlinedIcon from "@mui/icons-material/LocalOfferOutlined";
import { api } from "../../config/api";
import { ShippingMethod, ShoppingCartItem } from "../../types/userTypes";
import { toast, ToastContainer } from "react-toastify";
import { toastCustomize } from "../profilePage/profilAdmin/ProfileAdminAddProducts";

const Basket = () => {
  const navigate = useNavigate();

  const [openDiscount, setOpenDiscount] = useState(false);
  const [step, setStep] = useState<number>(1);
  const [discountCode, setDiscountCode] = useState("");
  const [shippingMethods, setShippingMethods] = useState<
    ShippingMethod[] | null
  >(null);

  const { data: cart, refetch } = useCartByUserId();

  const changeQuantity = async (productItemId: number, operation: string) => {
    try {
      await api.put(
        `/api/v1/cart/update/products/${productItemId}/quantity/${operation}`
      );
      refetch();
    } catch (error) {
      console.error(error);
    }
  };

  const deleteProductItemFromCart = async (productItemId: number) => {
    try {
      await api.delete(`/api/v1/cart/delete/product/${productItemId}`);
      refetch();
    } catch (error) {
      console.error(error);
    }
  };

  const shoppingCartItems: ShoppingCartItem[] = cart?.shoppingCartItems || [];

  const calculateTotalCartValue = () => {
    const totalWithoutDiscounts = shoppingCartItems.reduce(
      (total, cartItem) => {
        const selectedVariant =
          cartItem.productItem.productItemOneByColour.find(
            (variant) => variant.id === cartItem.productItem.productItemId
          );

        if (!selectedVariant) return total;

        const price = selectedVariant.price;
        const discount = selectedVariant.discount;
        const quantity = cartItem.qty;

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

  const calculateTotalWithoutDiscount = () => {
    const totalWithoutDiscounts = shoppingCartItems.reduce(
      (total, cartItem) => {
        const selectedVariant =
          cartItem.productItem.productItemOneByColour.find(
            (variant) => variant.id === cartItem.productItem.productItemId
          );

        if (!selectedVariant) return total;

        const price = selectedVariant.price;
        const discount = selectedVariant.discount;
        const quantity = cartItem.qty;

        const discountedPrice =
          discount > 0 ? price - price * (discount / 100) : price;

        return total + discountedPrice * quantity;
      },
      0
    );

    return totalWithoutDiscounts.toFixed(2);
  };

  const addDiscountCode = async () => {
    if (!cart) {
      console.error("Cart is not available");
      return;
    }

    try {
      await api.post(`/api/v1/cart/add-discount?discountCode=${discountCode}`);
      toast.success("Discount Code added successfully!", toastCustomize);
      setDiscountCode("");
      refetch();
    } catch (error: any) {
      console.log(error);
      toast.error(
        `Error adding discount code: ${error?.response?.data?.error}`,
        toastCustomize
      );
    }
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
      return method.price < min.price ? method : min;
    });

    return lowest.price;
  };

  return (
    <div>
      {step === 1 ? (
        <div>
          {cart?.shoppingCartItems && cart?.shoppingCartItems.length > 0 ? (
            <div className="w-full h-full py-6 px-16 max-md:px-4 max-md:py-2">
              <div className="flex gap-8 py-6 max-lg:flex-col max-lg:gap-4 max-md:py-2">
                <div className="w-[70%] max-lg:w-full">
                  <span>
                    Basket contains{" "}
                    <b>
                      {cart?.shoppingCartItems?.length}{" "}
                      {cart?.shoppingCartItems?.length === 1
                        ? "product"
                        : "products"}
                    </b>
                  </span>
                  <div className="py-4">
                    {cart?.shoppingCartItems?.map((product, index) => {
                      const selectedVariant =
                        product?.productItem?.productItemOneByColour?.find(
                          (variant) =>
                            variant.id === product?.productItem?.productItemId
                        );
                      const sizeVariation = selectedVariant?.variations?.find(
                        (v) => v.name === "size"
                      );

                      const matchedItem =
                        product?.productItem?.productItemOneByColour?.find(
                          (item) =>
                            item.id === product?.productItem?.productItemId
                        );
                      return (
                        <div
                          key={index}
                          className="w-full flex border-b-[1px] justify-between items-center px-8 border-gray-100 max-md:flex-col max-md:py-2 max-md:items-start max-md:px-2"
                        >
                          <div className="w-full flex gap-6 py-4 max-md:gap-2 max-md:py-0">
                            <img
                              src={
                                product?.productItem?.productImages[0]
                                  .imageFilename
                              }
                              alt={
                                product?.id + product?.productItem.productName
                              }
                              className="w-[100px] object-cover cursor-pointer"
                              onClick={() => {
                                const colourVariation =
                                  product?.productItem?.productItemOneByColour[0]?.variations.find(
                                    (v) => v.name === "colour"
                                  );
                                const colourValue =
                                  colourVariation?.options?.[0]?.value;

                                navigate(
                                  `/products/${product.productItem.productId}${
                                    colourValue && colourValue !== "Unknown"
                                      ? "-" +
                                        colourValue
                                          .toLowerCase()
                                          .replace(/\s+/g, "-")
                                      : ""
                                  }`
                                );
                              }}
                            />
                            <div className="flex flex-col gap-2 py-4 px-6">
                              <span className="text-lg font-semibold max-md:text-sm">
                                {product.productItem.productName}
                              </span>
                              {sizeVariation?.options?.[0]?.value && (
                                <span>
                                  Size:{" "}
                                  <b>
                                    {sizeVariation.options[0].value.toUpperCase()}
                                  </b>
                                </span>
                              )}
                              <div
                                className="flex items-center gap-[2px] cursor-pointer"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  deleteProductItemFromCart(
                                    product.productItem.productItemId
                                  );
                                }}
                              >
                                <DeleteForeverIcon />
                                <span>Delete</span>
                              </div>
                            </div>
                          </div>
                          <div className="w-full flex gap-12 items-center justify-between max-md:justify-between">
                            <div className="flex items-center gap-4 border-[1px] rounded-full py-[1px] px-6">
                              <span>{product?.qty}</span>
                              <div className="flex flex-col">
                                {matchedItem &&
                                matchedItem.qtyInStock > product?.qty ? (
                                  <KeyboardArrowUpOutlinedIcon
                                    className="cursor-pointer hover:opacity-30"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      changeQuantity(
                                        product.productItem.productItemId,
                                        "add"
                                      );
                                    }}
                                  />
                                ) : (
                                  <KeyboardArrowUpOutlinedIcon
                                    className="text-gray-400"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                    }}
                                  />
                                )}
                                {matchedItem && matchedItem.qtyInStock > 1 ? (
                                  <KeyboardArrowDownOutlinedIcon
                                    className="cursor-pointer hover:opacity-30"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      changeQuantity(
                                        product.productItem.productItemId,
                                        "delete"
                                      );
                                    }}
                                  />
                                ) : (
                                  <KeyboardArrowDownOutlinedIcon
                                    className="text-gray-400"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                    }}
                                  />
                                )}
                              </div>
                            </div>
                            <div className="flex flex-col items-center">
                              <span className="text-gray-500">Value</span>
                              <span>
                                {selectedVariant
                                  ? selectedVariant.discount > 0
                                    ? (
                                        (selectedVariant.price -
                                          (selectedVariant.price *
                                            selectedVariant.discount) /
                                            100) *
                                        product.qty
                                      ).toFixed(2)
                                    : (
                                        selectedVariant.price * product.qty
                                      ).toFixed(2)
                                  : "0.00"}{" "}
                                $
                              </span>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                    <div
                      className="w-max flex items-center gap-2 mt-6 p-4 border-[1px] border-black rounded-full cursor-pointer hover:bg-gray-500 hover:border-none hover:outline-[1px] group max-md:p-3"
                      onClick={() => navigate("/products?category=1")}
                    >
                      <WestOutlinedIcon className="transition-transform duration-300 ease-in-out transform group-hover:translate-x-[-5px] group-hover:text-white" />
                      <span className="font-bold group-hover:text-white max-md:text-sm">
                        Return to shopping
                      </span>
                    </div>
                  </div>
                </div>
                <div className="w-[30%] flex flex-col gap-6 max-lg:w-full">
                  <div className="w-full flex flex-col gap-6 p-4 cursor-pointer border-[2px] border-gray-200 bg-gray-100">
                    <div
                      className="flex items-center justify-between"
                      onClick={() => setOpenDiscount(!openDiscount)}
                    >
                      <div className="flex items-center gap-2">
                        <LocalOfferOutlinedIcon style={{ fontSize: 32 }} />
                        <span className="text-lg font-semibold max-md:text-base">
                          Use discount code
                        </span>
                      </div>
                      {!openDiscount ? (
                        <KeyboardArrowDownOutlinedIcon />
                      ) : (
                        <KeyboardArrowUpOutlinedIcon />
                      )}
                    </div>
                    {openDiscount && (
                      <div className="flex gap-2 max-md:flex-col">
                        <input
                          type="text"
                          placeholder="Enter code"
                          className="flex-grow outline-none border-[2px] border-gray-300 p-3"
                          onChange={(e) => setDiscountCode(e.target.value)}
                        />
                        <button
                          onClick={() => addDiscountCode()}
                          className="border-[2px] border-black rounded-full px-4 text-center font-bold bg-white hover:bg-gray-500 hover:border-transparent hover:outline-[2px] hover:text-white max-md:py-2"
                        >
                          Apply code
                        </button>
                      </div>
                    )}
                    {cart.discountCodes && cart.discountCodes.length > 0 && (
                      <div className="flex flex-grow gap-2">
                        {cart.discountCodes.map((code) => (
                          <div
                            key={code.id}
                            className="flex py-3 px-6 bg-green-400 font-bold rounded-full"
                          >
                            {code.code}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                  <div className="w-full flex flex-col gap-6 p-4 cursor-pointer border-[2px] border-gray-200">
                    <div className="flex justify-between items-center py-4 text-xl border-b-[1px] border-gray-100 text-gray-500 max-md:text-base">
                      <span>Products (total):</span>
                      <span>{calculateTotalWithoutDiscount()} $</span>
                    </div>
                    <div className="flex justify-between items-center py-4 text-xl border-b-[1px] border-gray-100 text-gray-500 max-md:text-base">
                      <span>Delivery:</span>
                      <div className="flex gap-2 items-center">
                        <LocalShippingIcon />
                        <span>from {getTheLowestShippingPrice()} $</span>
                      </div>
                    </div>
                    {cart?.discountCodes && cart?.discountCodes?.length > 0 && (
                      <div className="flex flex-col gap-4">
                        {cart.discountCodes.map((code) => (
                          <div
                            key={code.id}
                            className="flex justify-between items-center py-4 text-xl border-b-[1px] border-gray-100 text-gray-500 max-md:text-base"
                          >
                            <span>Discount: {code?.code}</span>
                            <span className="text-red-300 font-bold">
                              -{code?.discount}%
                            </span>
                          </div>
                        ))}
                      </div>
                    )}
                    <div className="flex flex-col gap-6 py-4 text-lg font-bold max-md:text-base">
                      <div className="flex items-center justify-between">
                        <span>Total value:</span>
                        <span>{calculateTotalCartValue()} $</span>
                      </div>
                      <button
                        className="h-[55px] w-full flex bg-black text-white rounded-3xl text-center items-center justify-center font-bold cursor-pointer"
                        onClick={() => navigate("/basket/delivery")}
                      >
                        To payment
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className="text-3xl font-bold text-center mt-12">
              Your basket is empty
            </div>
          )}
        </div>
      ) : (
        <div>
          <div></div>
        </div>
      )}
      <ToastContainer />
    </div>
  );
};

export default Basket;
