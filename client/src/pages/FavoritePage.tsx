import React, { useEffect, useState } from "react";
import { useFavorites, useToggleFavorite } from "../hooks/query";
import { api } from "../config/api";
import {
  ProductItemByColour,
} from "../types/userTypes";
import LoadingAnimation from "../ui/LoadingAnimation";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast, ToastContainer } from "react-toastify";
import { toastCustomize } from "../components/profilePage/profilAdmin/ProfileAdminAddProducts";
import { useNavigate } from "react-router";

const FavoritePage = () => {
  const navigate = useNavigate();
  const { data: favorites, isFetching } = useFavorites();
  const queryClient = useQueryClient();
  const toggleFavorite = useToggleFavorite();

  const [productItems, setProductItems] = useState<ProductItemByColour[] | []>(
    []
  );

  const [isHovered, setIsHovered] = useState<number | null>(null);

  useEffect(() => {
    if (favorites?.length === undefined || favorites?.length === 0) {
      return;
    }
    const getProductItemsByIds = async () => {
      try {
        const res = await api.get(
          `/api/v1/productItems/byProductItemIds?productItemIds=${favorites}`
        );
        setProductItems(res.data);
      } catch (error) {
        console.error(error);
      }
    };
    getProductItemsByIds();
  }, [favorites]);

  const handleToggleFavorite = async (productItemId: number) => {
    toggleFavorite.mutate(productItemId, {
      onSuccess: () => {
        setProductItems((prev) =>
          prev.filter((item) => item.productItemId !== productItemId)
        );
      },
      onError: (error) => {
        console.error("Error toggling favorite: ", error);
      },
    });
  };

  const mutation = useMutation({
    mutationFn: async (data: { productItemId: number; quantity: number }) => {
      const res = await api.post(
        `/api/v1/cart/products/${data.productItemId}/quantity/${data.quantity}`
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

  const addToCart = async (productItemId: number, quantity = 1) => {
    try {
      await mutation.mutateAsync({ productItemId, quantity });
      handleToggleFavorite(productItemId);
    } catch (error) {
      console.error("Error updating product:", error);
    }
  };

  return (
    <div className="relative flex flex-col pt-[120px] min-h-screen max-lg:pt-[100px]">
      {isFetching ? (
        <div className="absolute top-0 left-0 flex w-full h-full justify-center items-center bg-white opacity-60 z-50">
          <LoadingAnimation />
        </div>
      ) : (
        <div className="w-full flex gap-8 py-6 px-12 max-lg:px-6">
          <div className="w-full">
            <h1 className="text-2xl font-semibold uppercase">Favorite</h1>
            {productItems?.length === 0 ? (
              <div className="w-full flex justify-center gap-6 py-12 max-lg:py-6">
                <span className="text-gray-500">
                  There are no favorites products
                </span>
              </div>
            ) : (
              <div className="flex flex-wrap gap-6 py-12 max-lg:py-6">
                {productItems?.map((item) => {
                  const selectedVariation = item.productItemOneByColour.find(
                    (v) => v.id === item.productItemId
                  );
                  if (!selectedVariation) return null;
                  return (
                    <div
                      key={item.productItemId}
                      onMouseEnter={() => setIsHovered(item.productItemId)}
                      onMouseLeave={() => setIsHovered(null)}
                      onClick={() =>
                        setIsHovered((prev) =>
                          prev === item.productItemId
                            ? null
                            : item.productItemId
                        )
                      }
                      className="w-[250px] relative flex flex-col gap-2 cursor-pointer overflow-hidden max-lg:w-[200px] max-md:w-[130px]"
                    >
                      <img
                        src={item?.productImages[0]?.imageFilename}
                        alt=""
                        className="w-[250px] h-[350px] object-cover hover:scale-105 transition-transform duration-300 ease-in-out transform origin-center max-lg:w-full max-lg:h-full max-lg:object-contain"
                      />
                      <div className="flex flex-col max-md:text-sm">
                        <span>{item?.productName}</span>
                        {selectedVariation?.variations.find(
                          (variation) => variation?.name === "size"
                        )?.options[0]?.value && (
                          <span>
                            Size:{" "}
                            <b>
                              {
                                selectedVariation?.variations.find(
                                  (v) => v.name === "size"
                                )?.options[0]?.value
                              }
                            </b>
                          </span>
                        )}
                        {selectedVariation?.discount > 0 ? (
                          <div className="flex gap-2">
                            <span className="font-bold">
                              {(
                                selectedVariation?.price -
                                selectedVariation?.price * (selectedVariation?.discount / 100)
                              ).toFixed(2)}{" "}
                              $
                            </span>
                            <span className="text-gray-500 line-through">
                              {selectedVariation?.price?.toFixed(2)} $
                            </span>
                          </div>
                        ) : (
                          <span className="font-bold">{selectedVariation?.price} $</span>
                        )}
                      </div>
                      <div
                        className={`absolute bottom-0 left-0 right-0 h-[180px] bg-black bg-opacity-70 flex flex-col justify-center items-center gap-4 p-4 transition-all duration-300 ease-in-out transform max-md:h-[150px] max-lg:p-2 max-lg:gap-2 max-md:text-sm ${
                          isHovered === item.productItemId
                            ? "translate-y-0"
                            : "translate-y-full"
                        }`}
                      >
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleToggleFavorite(item.productItemId);
                          }}
                          className="w-full py-2 bg-white text-black rounded font-bold hover:bg-gray-200 transition-colors"
                        >
                          Delete
                        </button>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            addToCart(item.productItemId);
                          }}
                          className="w-full py-2 bg-black text-white rounded font-bold hover:bg-primary-dark transition-colors"
                        >
                          Add to cart
                        </button>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(
                              `/products/${item?.productId}${
                                item?.colour !== "Unknown"
                                  ? "-" + item?.colour
                                  : ""
                              }`
                            );
                          }}
                          className="w-full py-2 bg-black text-white rounded font-bold hover:bg-primary-dark transition-colors"
                        >
                          Go to product
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      )}
      <ToastContainer />
    </div>
  );
};

export default FavoritePage;
