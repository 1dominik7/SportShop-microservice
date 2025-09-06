import React, { useEffect, useState } from "react";
import { useFavorites, useToggleFavorite } from "../../hooks/query";
import { useNavigate } from "react-router";
import {
  ProductItemByColour,
  ProductItemOneByColour,
  ReviewRate,
  UserReviewProductById,
} from "../../types/userTypes";
import StraightenIcon from "@mui/icons-material/Straighten";
import FavoriteIcon from "@mui/icons-material/Favorite";
import { api } from "../../config/api";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import KeyboardArrowUpIcon from "@mui/icons-material/KeyboardArrowUp";
import FavoriteBorderIcon from "@mui/icons-material/FavoriteBorder";
import { useAppSelector } from "../../state/store";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import useIsAdmin from "../../hooks/useIsAdmin";
import { toast, ToastContainer } from "react-toastify";
import { toastCustomize } from "../profilePage/profilAdmin/ProfileAdminAddProducts";
import ProductRating from "../productsPage/ProductRating";
import ProductItemDetails from "./ProductItemDetails";

interface Props {
  data: ProductItemByColour | undefined;
  review: UserReviewProductById | null;
  productDescRef: React.RefObject<HTMLDivElement> | null;
  productDetailsRef: React.RefObject<HTMLDivElement> | null;
  productReviewsRef: React.RefObject<HTMLDivElement> | null;
  deliveryRef: React.RefObject<HTMLDivElement> | null;
  openSection: { [key: number]: boolean };
  setOpenSection: React.Dispatch<
    React.SetStateAction<{ [key: number]: boolean }>
  >;
}

const ProductItemRight = ({
  data,
  review,
  productDescRef,
  productDetailsRef,
  productReviewsRef,
  deliveryRef,
  openSection,
  setOpenSection,
}: Props) => {
  const auth = useAppSelector((store) => store.auth);
  const navigate = useNavigate();
  const isAdmin = useIsAdmin();
  const queryClient = useQueryClient();

  const [quantity, setQuantity] = useState<number>(1);
  const [productItem, selectedProductItem] = useState<
    ProductItemOneByColour | undefined
  >(undefined);
  const [errorMessage, setErrorMessage] = useState("");
  const [noSize, setNoSize] = useState(false);

  const { data: favorites } = useFavorites();
  const toggleFavorite = useToggleFavorite();

  const isFavorite =
    productItem?.id !== undefined ? favorites?.includes(productItem.id) : false;

  const mutation = useMutation({
    mutationFn: async (data: { productItemId: number; quantity: number }) => {
      const res = await api.post(
        `/api/v1/cart/products/${productItem?.id}/quantity/${data.quantity}`
      );
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["userCart"] });
      setTimeout(() => {
        toast.success("Product added to cart!", toastCustomize);
      }, 100);
    },
    onError: (error: Error) => {
      console.error("Error:", error);
    },
  });

  const addProductToCart = async () => {
    if (!productItem) {
      setNoSize(true);
      return;
    }

    if (!auth.isLoggedIn) {
      navigate("/login");
      return;
    }

    if (productItem.qtyInStock < quantity) {
      toast.error(
        "There is not enough quantity of product on stock!",
        toastCustomize
      );
      return;
    }

    try {
      await mutation.mutateAsync({ productItemId: productItem.id, quantity });
      setErrorMessage("");
    } catch (error: any) {
      const message = error?.response?.data?.error || "Something went wrong.";
      setErrorMessage(message);
      console.error("Error updating product:", error);
    }
  };

  const lowestPriceNumber = data?.productItemOneByColour?.reduce(
    (min, item) => {
      const discountedPrice = item.price * (1 - item.discount / 100);
      return discountedPrice < min ? discountedPrice : min;
    },
    Infinity
  );

  const lowestPrice =
    lowestPriceNumber !== undefined && lowestPriceNumber !== Infinity
      ? Number(lowestPriceNumber.toFixed(2))
      : "N/A";

  const reviewRate: ReviewRate = {
    productId: data?.productId || 0,
    totalReviews: review?.totalReviews || 0,
    averageRating: review?.averageRating || 0,
  };

  const handleToggleFavorite = () => {
    if (!productItem) {
      setNoSize(true);
      return;
    }

    toggleFavorite.mutate(productItem?.id);
  };

  useEffect(() => {
  setErrorMessage("");
}, [productItem, quantity]);

return (
    <div>
      <div className="h-full flex flex-col mt-6 gap-6 max-lg:gap-2 max-md:mt-0">
        <span className="text-3xl font-bold max-lg:text-xl">
          {data?.productName}
        </span>
        <div className="flex flex-col gap-4 max-md:gap-2">
          <div className="flex flex-col gap-[2px]">
            <span className="text-gray-400 capitalize">
              Product code:{" "}
              {productItem
                ? productItem?.productCode
                : data?.productItemOneByColour &&
                  data?.productItemOneByColour.length > 0
                ? data.productItemOneByColour[0]?.productCode
                : "No product code available"}
            </span>
            <span className="text-gray-400 capitalize">
              Colour: {data?.colour}
            </span>
          </div>
          {data?.productId && (
            <div className="flex gap-2 items-center text-lg">
              <ProductRating
                productId={data?.productId}
                reviews={[reviewRate]}
              />
            </div>
          )}
          <div className="flex gap-4 items-end">
            {!productItem ? (
              <span className="font-bold text-xl max-lg:text-base">
                from {lowestPrice} $
              </span>
            ) : (
              <div>
                {productItem.discount > 0 ? (
                  <div className="flex gap-4">
                    <span className="font-bold text-xl">
                      {(
                        productItem.price -
                        productItem.price * (productItem.discount / 100)
                      ).toFixed(2)}{" "}
                      $
                    </span>
                    <span className="text-gray-500 line-through">
                      {productItem.price.toFixed(2)} $
                    </span>
                  </div>
                ) : (
                  <span className="font-bold text-xl">
                    {productItem.price.toFixed(2)} $
                  </span>
                )}
              </div>
            )}
          </div>
        </div>
        {data?.otherProductItemOneByColours &&
          data.otherProductItemOneByColours.length > 0 && (
            <div className="flex flex-col gap-2">
              <span>Colour</span>
              <div className="flex gap-2 flex-wrap">
                {data?.otherProductItemOneByColours?.map((product, index) => (
                  <div
                    key={index}
                    className="cursor-pointer"
                    onClick={() => {
                      navigate(
                        `/products/${product?.productId}-${product?.colour}`
                      );
                      selectedProductItem(undefined);
                    }}
                  >
                    <div>
                      <img
                        className="h-[100px]"
                        src={
                          data?.otherProductItemOneByColours
                            ?.map((p) => p?.productImages[0]?.imageFilename)
                            .find((img) => img) || "/dummy_image.png"
                        }
                        alt={`${data?.productName}-${product?.colour}`}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        <div className="flex justify-between items-center">
          <span className="font-bold text-lg max-lg:text-base">
            Choose Size
          </span>
          <div className="flex items-center gap-2 cursor-pointer">
            <StraightenIcon />
            <span>Size chart</span>
          </div>
        </div>
        <div className="flex flex-wrap max-lg:mb-2">
          {data?.productItemOneByColour
            ?.filter((item) =>
              item.variations.some((variation) => variation.name === "size")
            )
            .sort((a, b) => {
              const sizeVariationA = a.variations.find(
                (variation) => variation.name === "size"
              );
              const sizeVariationB = b.variations.find(
                (variation) => variation.name === "size"
              );

              const sizeOrder = ["XS", "S", "M", "L", "XL", "XXL"];

              if (sizeVariationA && sizeVariationB) {
                const optionA = sizeVariationA.options[0]?.value.toUpperCase();
                const optionB = sizeVariationB.options[0]?.value.toUpperCase();

                const indexA = sizeOrder.indexOf(optionA);
                const indexB = sizeOrder.indexOf(optionB);

                if (indexA !== -1 && indexB !== -1) {
                  return indexA - indexB;
                }

                const numberA = parseFloat(optionA);
                const numberB = parseFloat(optionB);

                if (!isNaN(numberA) && !isNaN(numberB)) {
                  return numberA - numberB;
                }
              }
              return 0;
            })
            .map((item) => {
              const sizeVariation = item.variations.find(
                (variation) => variation.name === "size"
              );
              const sortedSizeOptions = sizeVariation?.options.sort(
                (a, b) => Number(a.value) - Number(b.value)
              );

              return (
                <div className="flex flex-col gap-2" key={item.id}>
                  <div className="flex flex-wrap gap-2">
                    {sizeVariation &&
                      sortedSizeOptions?.map((option) => (
                        <div
                          key={option.id}
                          className={`relative py-2 px-8 border-[1px] border-gray-300 cursor-pointer hover:bg-secondary-color hover:text-white ${
                            productItem?.id === item.id && "bg-secondary-color"
                          } ${
                            item?.qtyInStock === 0 &&
                            "bg-gray-100 opacity-70 text-gray-500"
                          } `}
                          onClick={() => {
                            if (item.qtyInStock > 0) {
                              selectedProductItem(item);
                              setNoSize(false);
                            }
                          }}
                        >
                          {item.qtyInStock < 1 && (
                            <>
                              <div className="top-[18px] -left-[4px] absolute w-[110%] bg-gray-300 h-[1px] rotate-[25deg]"></div>
                              <div className="top-[18px] -left-[4px] absolute w-[110%] bg-gray-300 h-[1px] -rotate-[25deg]"></div>
                            </>
                          )}
                          <span className="font-semibold relative z-10">
                            {option.value.toUpperCase()}
                          </span>
                        </div>
                      ))}
                  </div>
                </div>
              );
            })}
        </div>
        <div className="w-full h-[60px] flex justify-between gap-6 items-center max-lg:mb-2 max-lg:gap-2 max-lg:flex-wrap max-lg:h-full">
          <div className="w-[120px] h-full flex items-center px-5 rounded-3xl border-[1px] border-gray-300 max-lg:w-[100px]">
            <input
              type="text"
              inputMode="numeric"
              pattern="[0-9]*"
              min="0"
              step="1"
              className="w-full outline-none text-lg"
              onInput={(e: React.FormEvent<HTMLInputElement>) => {
                const target = e.target as HTMLInputElement;
                target.value = target.value.replace(/[^0-9]/g, "");
                setQuantity(Number(target.value));
              }}
              value={quantity}
            />
            <div className="flex flex-col">
              <KeyboardArrowUpIcon
                className="cursor-pointer text-gray-500"
                onClick={() => {
                  if (productItem && productItem?.qtyInStock > quantity)
                    setQuantity(quantity + 1);
                }}
              />
              <KeyboardArrowDownIcon
                className="cursor-pointer text-gray-500"
                onClick={() => {
                  if (quantity > 1) {
                    setQuantity(quantity - 1);
                  }
                }}
              />
            </div>
          </div>
          <button
            className="h-full w-full flex bg-black text-white rounded-3xl text-center items-center justify-center font-bold cursor-pointer max-lg:h-[50px]"
            onClick={addProductToCart}
          >
            Add to cart
          </button>
          <div
            className="w-[80px] h-full items-center justify-center flex border-2 border-gray-200 rounded-full cursor-pointer max-lg:w-full max-lg:h-[50px]"
            onClick={handleToggleFavorite}
            data-testid="favorite-toggle"
          >
            {isFavorite ? (
              <FavoriteIcon
                style={{ fontSize: 32 }}
                className="text-secondary-color"
              />
            ) : (
              <FavoriteBorderIcon
                style={{ fontSize: 32 }}
                className="text-secondary-color"
              />
            )}
          </div>
        </div>
        {noSize && <span className="text-red-500">Select Size</span>}
        {errorMessage && <span className="text-red-500">{errorMessage}</span>}
        {isAdmin && (
          <button
            className="w-full py-4 flex bg-yellow-500 text-white rounded-3xl text-center items-center justify-center font-bold cursor-pointer max-lg:h-[50px]"
            onClick={() => navigate(`/products/${data?.productId}/edit`)}
          >
            Edit Product
          </button>
        )}
        <div className="hidden max-md:flex mt-2">
          <ProductItemDetails
            data={data}
            review={review}
            productDescRef={productDescRef}
            productDetailsRef={productDetailsRef}
            productReviewsRef={productReviewsRef}
            deliveryRef={deliveryRef}
            openSection={openSection}
            setOpenSection={setOpenSection}
          />
        </div>
      </div>
      <ToastContainer />
    </div>
  );
};

export default ProductItemRight;
