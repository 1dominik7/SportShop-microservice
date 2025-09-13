import { useEffect, useRef, useState } from "react";
import {
  ProductItemByColour,
  UserReviewProductById,
} from "../../types/userTypes";
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/pagination";
import "swiper/css/autoplay";
import { Swiper, SwiperSlide } from "swiper/react";
import type { Swiper as SwiperType } from "swiper/types";
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

const ProductItemLeft = ({
  data,
  review,
  productDescRef,
  productDetailsRef,
  productReviewsRef,
  deliveryRef,
  openSection,
  setOpenSection,
}: Props) => {

  const swiperRef = useRef<SwiperType | null>(null);

  const [progress, setProgress] = useState(0);
  const [currentIndex, setCurrentIndex] = useState(0);
  const autoplayDelay = 5000;

  const moveToSlide = (index: number) => {
    if (swiperRef.current) {
      swiperRef.current.slideTo(index);
    }
  };

  const handleSlideChange = (swiper: SwiperType) => {
    setCurrentIndex(swiper.realIndex);
  };

  useEffect(() => {
    const step = 100 / (autoplayDelay / 40);
    const interval = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) return 0;
        return prev + step;
      });
    }, 40);

    return () => clearInterval(interval);
  }, [progress]);

  useEffect(() => {
    setProgress(0);
  }, [currentIndex]);

  return (
    <div>
      <div className="flex flex-col border-r-[1px] border-gray-200 max-md:border-r-0">
        <div className="w-full h-full flex flex-col gap-4 max-md:gap-2">
          <div className="flex gap-4 max-lg:flex-col-reverse">
            <div className="h-max flex w-[10%] flex-col gap-4 max-lg:flex-row max-lg:flex-wrap max-lg:w-full max-md:w-full">
              {data?.productImages?.length ? (
                data?.productImages.map((img, index) => (
                  <div
                    onClick={() => {
                      setCurrentIndex(index);
                      moveToSlide(index);
                    }}
                    className={`border-gray-200 border-[2px] cursor-pointer max-lg:w-[80px] max-md:w-[60px] ${
                      currentIndex === index ? "border-green-200" : ""
                    }`}
                    key={index}
                  >
                    <img src={img?.imageFilename} alt={`${index}`} className="w-full h-full object-cover"/>
                  </div>
                ))
              ) : (
                <p>No images available.</p>
              )}
            </div>
            {data?.productImages?.length ? (
              <Swiper
                spaceBetween={50}
                slidesPerView={1}
                pagination={{ clickable: true }}
                navigation={{
                  nextEl: ".swiper-button-next",
                  prevEl: ".swiper-button-prev",
                }}
                onSlideChange={handleSlideChange}
                onSwiper={(swiperInstance: SwiperType) => {
                  swiperRef.current = swiperInstance;
                }}
                initialSlide={currentIndex}
                className="h-full w-[100%] bg-blue"
              >
                {data?.productImages?.map((item, index) => (
                  <SwiperSlide key={index} className="h-full">
                    <img
                      src={item?.imageFilename}
                      alt={`${item?.imageFilename}`}
                      className="w-full h-[750px] object-cover max-lg:object-contain max-lg:h-[500px] max-md:h-[300px]"
                    />
                  </SwiperSlide>
                ))}
                <div
                  className="swiper-button-next"
                  onClick={() => swiperRef.current?.slideNext()}
                ></div>
                <div
                  className="swiper-button-prev"
                  onClick={() => swiperRef.current?.slidePrev()}
                ></div>
              </Swiper>
            ) : (
              <p>No images available</p>
            )}
          </div>
          <div className="w-[90%] flex flex-col gap-4">
            <div className="flex max-md:hidden">
              <div
                onClick={() => {
                  setOpenSection((prev) => ({
                    ...prev,
                    [1]: true,
                  }));
                  window.scrollTo({
                    top: productDescRef?.current!.offsetTop,
                    behavior: "smooth",
                  });
                  setTimeout(() => {
                    if (productDetailsRef?.current) {
                      window.scrollTo({
                        top: productDetailsRef.current.offsetTop,
                        behavior: "smooth",
                      });
                    }
                  }, 200);
                }}
                className="w-[100%] text-center cursor-pointer py-6 border-b-white border-b-2 hover:border-b-black hover:font-bold hover:border-b-2"
              >
                <span className="text-lg max-lg:text-base">
                  Product description
                </span>
              </div>
              <div
                onClick={() => {
                  setOpenSection((prev) => ({
                    ...prev,
                    [2]: true,
                  }));
                  setTimeout(() => {
                    if (productDetailsRef?.current) {
                      window.scrollTo({
                        top: productDetailsRef.current.offsetTop,
                        behavior: "smooth",
                      });
                    }
                  }, 200);
                }}
                className="w-[100%] text-center cursor-pointer py-6 border-b-black hover:font-bold hover:border-b-2"
              >
                <span className="text-lg max-lg:text-base">
                  Product details
                </span>
              </div>
              <div
                onClick={() => {
                  setOpenSection((prev) => ({
                    ...prev,
                    [3]: true,
                  }));
                  window.scrollTo({
                    top: productReviewsRef?.current!.offsetTop,
                    behavior: "smooth",
                  });
                  setTimeout(() => {
                    if (productDetailsRef?.current) {
                      window.scrollTo({
                        top: productDetailsRef.current.offsetTop,
                        behavior: "smooth",
                      });
                    }
                  }, 200);
                }}
                className="w-[100%] text-center cursor-pointer py-6 border-b-black hover:font-bold hover:border-b-2"
              >
                <span className="text-lg max-lg:text-base">
                  Product reviews
                </span>
              </div>
              <div
                onClick={() => {
                  setOpenSection((prev) => ({
                    ...prev,
                    [4]: true,
                  }));
                  window.scrollTo({
                    top: deliveryRef?.current!.offsetTop,
                    behavior: "smooth",
                  });
                  setTimeout(() => {
                    if (productDetailsRef?.current) {
                      window.scrollTo({
                        top: productDetailsRef.current.offsetTop,
                        behavior: "smooth",
                      });
                    }
                  }, 200);
                }}
                className="w-[100%] text-center cursor-pointer py-6 border-b-black hover:font-bold hover:border-b-2"
              >
                <span className="text-lg max-lg:text-base">Delivery</span>
              </div>
            </div>
          </div>
        </div>
        <div className="w-full border-t-[1px] border-t-gray-200 max-md:hidden">
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
    </div>
  );
};

export default ProductItemLeft;
