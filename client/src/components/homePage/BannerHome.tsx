import { useEffect, useRef, useState } from "react";
import { Swiper, SwiperSlide } from "swiper/react";
import { Swiper as SwiperType, Autoplay } from "swiper";
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/pagination";
import "swiper/css/autoplay";
import LoadingAnimation from "../../ui/LoadingAnimation";
import { api } from "../../config/api";
import { useQuery } from "@tanstack/react-query";

interface Image {
  id: number;
  url: string;
  name?: string;
  displayOrder?: number;
}

const BannerHome = () => {
  const swiperRef = useRef<SwiperType | null>(null);

  const [progress, setProgress] = useState(0);
  const [currentIndex, setCurrentIndex] = useState(0);
  const autoplayDelay = 5000;

  const fetchBannerImages = async (): Promise<Image[]> => {
    try {
      const res = await api.get("/api/v1/main-images/with-order", {
        headers: {
          "Access-Control-Allow-Origin": "*",
        },
      });

      return res.data;
    } catch (error) {
      console.error("Error fetching banner images:", error);
      throw error;
    }
  };

  const {
    data: images,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["bannerImages"],
    queryFn: fetchBannerImages,
  });

  const moveToSlide = (index: number) => {
    if (swiperRef.current) {
      swiperRef.current.slideTo(index + 1);
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
    <div className="relative h-screen">
      {isLoading && (
        <div className="absolute w-full h-full bg-gray-300 bg-opacity-20 flex justify-center items-center z-10">
          <LoadingAnimation />
        </div>
      )}

      <Swiper
        spaceBetween={50}
        slidesPerView={1}
        loop={true}
        autoplay={{
          delay: autoplayDelay,
          disableOnInteraction: false,
          waitForTransition: true,
        }}
        pagination={{ clickable: true }}
        onSlideChange={handleSlideChange}
        onSwiper={(swiperInstance: SwiperType) => {
          swiperRef.current = swiperInstance;
        }}
        className="h-full"
      >
        {images?.map((item) => (
          <SwiperSlide key={item.id} className="h-full">
            <img
              src={item?.url}
              alt={item?.name}
              className="w-full h-full object-cover"
            />
          </SwiperSlide>
        ))}
      </Swiper>
      <div className="absolute flex w-full items-center justify-center space-x-36 bottom-10 text-white z-20 max-xl:space-x-10 max-md:flex-col max-md:space-y-6 max-md:space-x-0">
        {images?.map((item, index) => (
          <div
            onClick={() => moveToSlide(index)}
            key={index}
            className="cursor-pointer text-2xl font-semibold"
          >
            <div className="relative flex flex-col items-center">
              <span className="text-base">{item.name}</span>
              <div className="relative h-[2px] bg-gray-400 mt-2 w-[120px] max-xl:w-[80px]">
                <div
                  style={{
                    width: `${currentIndex === index ? 100 : 0}%`,
                    animation:
                      currentIndex === index
                        ? `progressBar ${autoplayDelay}ms linear`
                        : "none",
                  }}
                  className="h-full bg-white"
                />
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default BannerHome;
