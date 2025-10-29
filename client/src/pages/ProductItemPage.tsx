import { useEffect, useRef, useState } from "react";
import ProductItemLeft from "../components/productItemPage/ProductItemLeft";
import ProductItemRight from "../components/productItemPage/ProductItemRight";
import { useParams } from "react-router";
import { useProductItemByProductIdAndColour } from "../hooks/query";
import { api } from "../config/api";
import { UserReviewProductById } from "../types/userTypes";
import LoadingAnimation from "../ui/LoadingAnimation";

const ProductItemPage = () => {
  const { productId } = useParams<{ productId: any }>();
  const [productItemId, colour] = productId.split("-");
  const [review, setReview] = useState<UserReviewProductById | null>(null);
  const productDescRef = useRef<HTMLDivElement | null>(null);
  const productDetailsRef = useRef<HTMLDivElement | null>(null);
  const productReviewsRef = useRef<HTMLDivElement | null>(null);
  const deliveryRef = useRef<HTMLDivElement | null>(null);
  const [openSection, setOpenSection] = useState<{ [key: number]: boolean }>({
    1: false,
    2: false,
    3: false,
    4: false,
  });

  const { data } = useProductItemByProductIdAndColour(productItemId, colour);

  useEffect(() => {
    if (data?.productId) {
      const getUserReviewById = async () => {
        try {
          const res = await api.get(
            `/api/v1/review/productById/${data.productId}`
          );
          setReview(res.data);
        } catch (error) {
          console.error(error);
        }
      };
      getUserReviewById();
    }
  }, [data]);

  return (
    <div className="relative flex flex-col pt-[120px] min-h-screen max-lg:pt-[100px]">
      <div className="relative w-full flex gap-8 py-6 px-12 max-lg:px-6 max-lg:gap-2 max-md:flex-col">
             {!data && 
        <div className="absolute top-0 left-0 flex w-full h-full justify-center items-center bg-white opacity-60 z-50">
              <LoadingAnimation/>
        </div>
      }
        <div className="w-[60%] max-md:w-full">
          <ProductItemLeft
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
        <div className="w-[40%] max-md:w-full">
          <ProductItemRight
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
    </div>
  );
};

export default ProductItemPage;
