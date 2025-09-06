import {
  ProductItemByColour,
  UserReviewProductById,
} from "../../types/userTypes";
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/pagination";
import "swiper/css/autoplay";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import { shippingData } from "../../utils/shippingTable";
import { Rating } from "@mui/material";

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

const ProductItemDetails = ({
  data,
  review,
  productDescRef,
  productDetailsRef,
  productReviewsRef,
  deliveryRef,
  openSection,
  setOpenSection,
}: Props) => {

  const toggleSection = (sectionId: 1 | 2 | 3 | 4) => {
    setOpenSection((prev) => ({
      ...prev,
      [sectionId]: !prev[sectionId],
    }));
  };

  const productWithMaxVariations = data?.productItemOneByColour
  ?.filter((product) => product.variations.length > 0)
  .reduce((maxProduct, currentProduct) => {
    return currentProduct.variations.length > maxProduct.variations.length
      ? currentProduct
      : maxProduct;
  }, data?.productItemOneByColour[0] || {});


  return (
    <div className="w-full border-t-[1px] border-t-gray-200">
      <div>
        <div
          ref={productDescRef}
          className="flex flex-col justify-between cursor-pointer border-b-gray-200 border-b-[2px]"
        >
          <div
            onClick={() => toggleSection(1)}
            className="flex justify-between items-center p-6 max-lg:p-4"
          >
            <span className="font-bold text-3xl max-lg:text-2xl max-md:text-xl">
              Product description
            </span>
            <KeyboardArrowDownIcon />
          </div>
          {openSection[1] && (
            <div className="pb-2 pt-6 px-6 max-lg:pt-0 max-lg:px-4 max-md:text-sm">
              <span className="text-gray-400">
                {data &&
                data.productItemOneByColour &&
                data.productItemOneByColour.length > 0
                  ? data.productItemOneByColour[0].productDescription
                  : "No description available"}
              </span>
            </div>
          )}
        </div>
        <div
          ref={productDetailsRef}
          className="flex flex-col justify-between cursor-pointer border-b-gray-200 border-b-[2px]"
        >
          <div
            className="flex justify-between items-center p-6 max-lg:p-4"
            onClick={() => toggleSection(2)}
          >
            <span className="font-bold text-3xl max-lg:text-2xl max-md:text-xl">
              Product details
            </span>
            <KeyboardArrowDownIcon />
          </div>
          {openSection[2] && (
            <div className="pb-2 pt-6 px-6 max-lg:w-full max-lg:pt-0 max-lg:px-4 max-md:text-sm">
              {productWithMaxVariations &&
                productWithMaxVariations?.variations?.map((variation) => (
                  <div
                    key={variation.id}
                    className="flex gap-6 odd:bg-gray-200 py-4 px-6 max-lg:py-2 max-lg:px-4 max-md:gap-2 max-md:justify-between"
                  >
                    <span className="w-[200px] capitalize max-md:w-max">
                      {variation.name}:
                    </span>
                    <div className="flex">
                      {variation?.options?.map((option, index) => (
                        <span key={option.id} className="capitalize">
                          {option.value}
                          {index < variation.options.length - 1 && ", "}
                        </span>
                      ))}
                    </div>
                  </div>
                ))}
            </div>
          )}
        </div>
        <div
          ref={productReviewsRef}
          className="flex flex-col justify-between cursor-pointer border-b-gray-200 border-b-[2px]"
        >
          <div
            className="flex justify-between items-center p-6 max-lg:p-4"
            onClick={() => toggleSection(3)}
          >
            <span className="font-bold text-3xl max-lg:text-2xl max-md:text-xl">
              Product reviews
            </span>
            <KeyboardArrowDownIcon />
          </div>
          {openSection[3] && (
            <div className="pb-2 pt-6 px-6 max-lg:pt-0 max-lg:px-2 max-md:text-sm">
              <div className="text-gray-400 overflow-y-scroll">
                {review !== null ? (
                  <div className="flex flex-col gap-4 max-h-[400px] overflow-y-scroll p-4 text-black">
                    {review.reviews.map((rev) => (
                      <div
                        key={rev.id}
                        className="flex flex-col gap-4 p-6 bg-white border-black border-[1px] max-lg:gap-2 max-lg:p-4"
                      >
                        <div className="flex items-center gap-2">
                          <span className="font-bold">{rev.userName}</span>
                          <Rating value={rev.ratingValue} readOnly />
                        </div>
                        <p>{rev.comment}</p>
                        <div className="flex items-center gap-2">
                          <span className="font-bold">Created on:</span>
                          <span>
                            {new Date(rev.createdDate).toLocaleDateString(
                              "en-GB"
                            )}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <span className="text-xl">There are no reviews yet!</span>
                )}
              </div>
            </div>
          )}
        </div>
        <div
          ref={deliveryRef}
          className="flex flex-col justify-between cursor-pointer border-b-gray-200 border-b-[2px]"
        >
          <div
            className="flex justify-between items-center p-6 max-lg:p-4"
            onClick={() => toggleSection(4)}
          >
            <span className="font-bold text-3xl max-lg:text-2xl max-md:text-xl">Delivery</span>
            <KeyboardArrowDownIcon />
          </div>
          {openSection[4] && (
            <div className="pb-2 pt-6 px-6 text-sm max-lg:pt-2 max-lg:px-4">
              <div className="overflow-x-auto">
                <table className="min-w-full table-auto">
                  <thead>
                    <tr className="bg-gray-200 text-left">
                      <th className="px-4 py-2 border">Shipping Method</th>
                      <th className="px-4 py-2 border">
                        Order Processing Time
                      </th>
                      <th className="px-4 py-2 border">
                        Expected Delivery Time
                      </th>
                      <th className="px-4 py-2 border">Order Value</th>
                      <th className="px-4 py-2 border">Delivery Cost</th>
                      <th className="px-4 py-2 border">
                        COD (Cash on Delivery)
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {shippingData.map((row, index) => (
                      <tr key={index} className="border-b">
                        <td className="px-4 py-2 max-lg:px-2">{row.method}</td>
                        <td className="px-4 py-2 max-lg:px-2">
                          {row.processingTime}
                        </td>
                        <td className="px-4 py-2 max-lg:px-2">
                          {row.deliveryTime}
                        </td>
                        <td className="px-4 py-2 max-lg:px-2">
                          {row.orderValue}
                        </td>
                        <td className="px-4 py-2 max-lg:px-2">
                          {row.deliveryCost}
                        </td>
                        <td className="px-4 py-2 max-lg:px-2">{row.cod}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProductItemDetails;
