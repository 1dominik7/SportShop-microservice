import React, { useState } from "react";
import { useProductsByFiltersGrouped } from "../../../hooks/query";
import { useNavigate, useSearchParams } from "react-router";
import LoadingAnimation from "../../../ui/LoadingAnimation";
import { ToastContainer } from "react-toastify";
import { Pagination } from "@mui/material";
import ImageLoader from "../../../ui/ImageLoader";

const ProfileAdminProducts = () => {
  const navigate = useNavigate();

  const [searchParams, setSearchParams] = useSearchParams();

  const [pageNumber, setPageNumber] = useState(
    searchParams.get("page") ? parseInt(searchParams.get("page")!) : 0
  );

  const { data, isLoading } = useProductsByFiltersGrouped(
    undefined,
    {},
    pageNumber
  );

  const handleProductClick = (productId: number, productColour: string) => {
    navigate(
      `/products/${productId}${
        productColour !== "Unknown" ? "-" + productColour : ""
      }`
    );
  };

  const onChangeHandler = (
    event: React.ChangeEvent<unknown>,
    value: number
  ) => {
    setPageNumber(value - 1);
    setSearchParams({ page: `${value - 1}` });
  };

  return (
    <div className="w-full h-full pb-20 max-md:items-center p-6">
      {isLoading ? (
        <div className="flex items-center justify-center w-full h-full z-50 bg-white opacity-80">
          <LoadingAnimation />
        </div>
      ) : (
        <>
          <h1 className="font-bold text-3xl max-md:text-xl px-2">Products</h1>
          <div className="flex flex-wrap gap-10 p-2 justify-start max-md:justify-between max-md:gap-4">
            {data?.content.map((product, index) => (
              <div
                key={index}
                className="w-[22%] h-[400px] flex flex-col cursor-pointer overflow-hidden max-lg:h-[320px] max-lg:w-[25%] max-md:w-[29%] max-md:h-max max-md:text-sm"
                onClick={() =>
                  handleProductClick(product?.productId, product?.colour ?? "")
                }
              >
                <ImageLoader
                  className="w-full h-full object-cover hover:scale-105 transition-transform duration-300 ease-in-out transform origin-center max-md:object-contain"
                  src={
                    product?.productImages[0]?.imageFilename ||
                    "/dummy_image.png"
                  }
                  alt={product?.productImages[0]?.imageFilename}
                />
                <div className="w-full z-10 bg-white">
                  <span>{product?.productName}</span>
                  {product.colour && (
                    <span className="capitalize"> - {product?.colour}</span>
                  )}
                </div>
                {product.productItemRequests.some(
                  (item) => item.discount > 0
                ) ? (
                  <div className="flex gap-4 max-lg:flex-col max-lg:gap-0">
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
                        ...product.productItemRequests.map((item) => item.price)
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
            ))}
            <div className="w-full flex justify-center pt-10 items-center">
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
          </div>
        </>
      )}
      <ToastContainer />
    </div>
  );
};

export default ProfileAdminProducts;
