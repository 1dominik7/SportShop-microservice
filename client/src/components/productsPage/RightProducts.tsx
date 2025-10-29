import React, { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router";
import {
  ProductItemListGroupedByFilters,
  ProductItemsFilters,
  ReviewRate,
} from "../../types/userTypes";
import CancelIcon from "@mui/icons-material/Cancel";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import { Pagination } from "@mui/material";
import { api } from "../../config/api";
import ProductRating from "./ProductRating";

type SelectedOption = {
  [key: number]: { optionId: number[]; filteredName: string[] };
};

interface Props {
  pageSize: string;
  setPageSize: React.Dispatch<React.SetStateAction<string>>;
  pageNumber: number;
  setPageNumber: React.Dispatch<React.SetStateAction<number>>;
  sortBy: string;
  setSortBy: React.Dispatch<React.SetStateAction<string>>;
  sortOrder: string;
  setSortOrder: React.Dispatch<React.SetStateAction<string>>;
  selectedOption: SelectedOption;
  setSelectedOption: React.Dispatch<React.SetStateAction<SelectedOption>>;
  data: ProductItemListGroupedByFilters;
  filters: ProductItemsFilters[] | undefined;
}

const RightProducts = ({
  pageSize,
  setPageSize,
  pageNumber,
  setPageNumber,
  sortBy,
  setSortBy,
  sortOrder,
  setSortOrder,
  selectedOption,
  setSelectedOption,
  data,
  filters,
}: Props) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [openPageSize, setOpenPageSize] = useState(false);
  const [openSort, setOpenSort] = useState(false);
  const [reviews, setReviews] = useState<ReviewRate[] | []>([]);
  const [imagesLoaded, setImagesLoaded] = useState(false);
  const imageRefs = useRef<(HTMLImageElement | null)[]>([]);

  const onChangeHandler = (
    event: React.ChangeEvent<unknown>,
    value: number
  ) => {
    setPageNumber(value - 1);
  };

  const handleRemoveFilter = (
    variationId: number,
    optionId: number,
    filteredName: string
  ) => {
    setSelectedOption((prev) => {
      const newSelection = { ...prev };
      const currentOptionIds = newSelection[variationId]?.optionId || [];

      setPageNumber(0);

      if (currentOptionIds.includes(optionId)) {
        newSelection[variationId].optionId = currentOptionIds.filter(
          (id) => id !== optionId
        );
        newSelection[variationId].filteredName = newSelection[
          variationId
        ].filteredName.filter((name) => name !== filteredName);

        if (newSelection[variationId].optionId.length === 0) {
          delete newSelection[variationId];
        }
      }

      return newSelection;
    });
  };

  const handleClearFilters = () => {
    setSelectedOption({});

    const searchParams = new URLSearchParams(location.search);
    searchParams.delete("filters");

    const newUrl = `${window.location.pathname}?${searchParams.toString()}`;
    navigate(newUrl, { replace: true });
  };

  useEffect(() => {
    localStorage.setItem("pageSize", pageSize);
    localStorage.setItem("sortBy", sortBy);
    localStorage.setItem("sortOrder", sortOrder);
  }, [pageSize, sortBy, sortOrder]);

  useEffect(() => {
    if (!data?.content?.length) {
      return;
    }

    const productIds = data.content.map((product) => product.productId);

    const getProductsReviews = async () => {
      try {
        const res = await api.get(
          `/api/v1/review/product-summary?productIds=${productIds.join(",")}`
        );
        setReviews(res.data);
      } catch (error) {
        console.error(error);
      }
    };
    getProductsReviews();
  }, [data.content]);

  useEffect(() => {
    if (data?.content?.length) {
      let loadedCount = 0;
      const totalImages = data.content.length;

      const checkAllLoaded = () => {
        loadedCount++;
        if (loadedCount === totalImages) {
          setImagesLoaded(true);
        }
      };

      imageRefs.current.forEach((img) => {
        if (img?.complete) {
          checkAllLoaded();
        }
      });
    }
  }, [data]);

  return (
    <div className="w-full py-6 px-12 max-lg:px-6">
      {data.content.length > 0 ? (
        <>
          <div className="flex flex-col pb-4 gap-4">
            <div className="flex flex-col gap-2">
              <div className="w-full flex justify-between items-center max-lg:flex-col">
                {Object.keys(selectedOption).length > 0 ? (
                  <div className="w-full pt-2 flex gap-[6px]">
                    <span>Founded</span>
                    <span className="font-bold">
                      {data?.totalElements} products
                    </span>
                  </div>
                ) : (
                  <div></div>
                )}
                <div className="flex gap-6 items-center justify-end max-lg:justify-between w-full max-lg:flex-row-reverse">
                  <div className="flex items-center gap-3 max-lg:text-sm max-lg:flex-col max-lg:items-start">
                    <span>Products on page:</span>
                    <div className="relative p-3 border-[1px] border-black">
                      <div
                        className="flex gap-2 items-center justify-between cursor-pointer"
                        onClick={() => setOpenPageSize(!openPageSize)}
                      >
                        <span>{pageSize}</span>
                        <KeyboardArrowDownIcon
                          style={{ fontSize: 20 }}
                          className={`transform transition-transform duration-300 ease-in-out ${
                            openPageSize
                              ? "animate-rotate"
                              : "animate-rotate-reverse"
                          }`}
                        />
                      </div>
                      {openPageSize && (
                        <div
                          className={`absolute opacity-0 w-full flex flex-col gap-2 top-[48px] left-0 cursor-pointer shadow-lg bg-white z-20 transform transition-transform duration-300 ease-in-out ${
                            openPageSize
                              ? "opacity-100 translate-y-0"
                              : "opacity-0 translate-y-4"
                          }`}
                        >
                          <span
                            className="hover:bg-blue-400 px-3 py-[6px]"
                            onClick={() => {
                              setPageSize("24");
                              setOpenPageSize(!openPageSize);
                            }}
                          >
                            24
                          </span>
                          <span
                            className="hover:bg-blue-400 px-3 py-[6px]"
                            onClick={() => {
                              setPageSize("36");
                              setOpenPageSize(!openPageSize);
                            }}
                          >
                            36
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-3 max-lg:text-sm max-lg:flex-col max-lg:items-start">
                    <span>Sort by:</span>
                    <div className="relative p-3 border-[1px] border-black">
                      <div
                        className="flex min-w-[150px] gap-2 items-center justify-between cursor-pointer"
                        onClick={() => setOpenSort(!openSort)}
                      >
                        <span>
                          {sortBy === "productId" && sortOrder === "asc"
                            ? "The Oldest"
                            : sortBy === "productId" && sortOrder === "desc"
                            ? "The Newest"
                            : sortBy === "price" && sortOrder === "desc"
                            ? "The Highest Price"
                            : "The Lowest Price"}
                        </span>
                        <KeyboardArrowDownIcon
                          style={{ fontSize: 20 }}
                          className={`transform transition-transform duration-300 ease-in-out ${
                            openSort
                              ? "animate-rotate"
                              : "animate-rotate-reverse"
                          }`}
                        />
                      </div>
                      {openSort && (
                        <div
                          className={`absolute opacity-0 w-full flex flex-col gap-2 top-[48px] left-0 cursor-pointer shadow-lg z-20 bg-white transform transition-transform duration-300 ease-in-out ${
                            openSort
                              ? "opacity-100 translate-y-0"
                              : "opacity-0 translate-y-4"
                          }`}
                        >
                          <span
                            className="hover:bg-blue-400 px-3 py-[6px]"
                            onClick={() => {
                              setSortBy("productId");
                              setSortOrder("asc");
                              setOpenSort(!openSort);
                            }}
                          >
                            The Oldest
                          </span>
                          <span
                            className="hover:bg-blue-400 px-3 py-[6px]"
                            onClick={() => {
                              setSortBy("productId");
                              setSortOrder("desc");
                              setOpenSort(!openSort);
                            }}
                          >
                            The Newest
                          </span>
                          <span
                            className="hover:bg-blue-400 px-3 py-[6px]"
                            onClick={() => {
                              setSortBy("price");
                              setSortOrder("asc");
                              setOpenSort(!openSort);
                            }}
                          >
                            The Lowest Price
                          </span>
                          <span
                            className="hover:bg-blue-400 px-3 py-[6px]"
                            onClick={() => {
                              setSortBy("price");
                              setSortOrder("desc");
                              setOpenSort(!openSort);
                            }}
                          >
                            The Highest Price
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
              {Object.keys(selectedOption).length > 0 && (
                <div className="w-full flex-wrap flex items-center gap-3 max-lg:text-sm">
                  <span className="font-semibold mr-2">Active filters</span>
                  {filters &&
                    filters.map((item) => {
                      const variation = item.variation;
                      const selectedOptions =
                        selectedOption[variation.id]?.optionId || [];

                      return selectedOptions.map((optionId) => {
                        const option = variation.options.find(
                          (opt) => opt.id === optionId
                        );
                        if (!option) return null;

                        return (
                          <div
                            key={option.id}
                            onClick={() =>
                              handleRemoveFilter(
                                variation.id,
                                option.id,
                                variation.name
                              )
                            }
                          >
                            <div className="flex items-center cursor-pointer group">
                              <div className="flex items-center gap-[3px]">
                                <CancelIcon
                                  style={{ fontSize: 20 }}
                                  className="group-hover:text-gray-500"
                                />
                                <span className="group-hover:text-gray-500 capitalize">
                                  {option.value}
                                </span>
                              </div>
                            </div>
                          </div>
                        );
                      });
                    })}
                  <div
                    className="flex items-center gap-[4px] cursor-pointer group"
                    onClick={handleClearFilters}
                  >
                    <CancelIcon
                      style={{ fontSize: 20 }}
                      className="group-hover:text-gray-500"
                    />
                    <span className="group-hover:text-gray-500">
                      Clear filters
                    </span>
                  </div>
                </div>
              )}
            </div>
          </div>
          <div className="flex flex-wrap gap-10 max-lg:gap-4 max-lg:text-sm">
            {data?.content?.map((product, index) => {
              return (
                <div
                  key={index}
                  className="w-[22%] h-[400px] flex flex-col cursor-pointer overflow-hidden max-lg:h-full max-lg:w-[45%]"
                  onClick={() =>
                    navigate(
                      `${product?.productId}${
                        product?.colour !== ""
                          ? "-" + product?.colour
                          : ""
                      }`
                    )
                  }
                >
                  <img
                    className="w-full h-full object-cover hover:scale-105 transition-transform duration-300 ease-in-out transform origin-center"
                    src={
                      product?.productImages[0]?.imageFilename ||
                      "/dummy_image.png"
                    }
                    alt={product?.productImages[0]?.imageFilename}
                    onLoad={() => {
                      const img = imageRefs.current[index];
                      if (img?.complete) {
                        if (imageRefs.current.every((ref) => ref?.complete)) {
                          setImagesLoaded(true);
                        }
                      }
                    }}
                    loading="lazy"
                  />
                  <div className="w-full z-10 bg-white">
                    <span>{product?.productName}</span>
                    {product.colour !== "Unknown" && (
                      <span className="capitalize"> - {product?.colour}</span>
                    )}
                    <ProductRating
                      productId={product.productId}
                      reviews={reviews}
                    />
                  </div>
                  {product.productItemRequests.some(
                    (item) => item.discount > 0
                  ) ? (
                    <div className="flex gap-4 max-lg:flex-col max-lg:gap-[2px]">
                      <span className="font-bold">
                        from{" "}
                        {Math.min(
                          ...product.productItemRequests.map(
                            (item) =>
                              item.price - item.price * (item.discount / 100)
                          )
                        ).toFixed(2)}{" "}
                        $
                      </span>
                      <span className="text-gray-500 line-through">
                        {Math.max(
                          ...product.productItemRequests.map(
                            (item) => item.price
                          )
                        ).toFixed(2)}{" "}
                        $
                      </span>
                    </div>
                  ) : (
                    <span className="font-bold">
                      from{" "}
                      {Math.min(
                        ...product.productItemRequests.map((item) => item.price)
                      ).toFixed(2)}{" "}
                      $
                    </span>
                  )}
                </div>
              );
            })}
          </div>
          <div className="flex justify-center pt-10">
            <Pagination
              count={data?.totalPages}
              page={pageNumber + 1}
              defaultPage={1}
              siblingCount={0}
              boundaryCount={2}
              shape="rounded"
              onChange={onChangeHandler}
            />
          </div>
        </>
      ) : (
        <div className="flex gap-2 items-center p-4">
          <div className="h-4 w-4 bg-secondary-color rounded-full" />
          <span className="font-bold text-xl">There is no products {":("}</span>
        </div>
      )}
    </div>
  );
};

export default RightProducts;
