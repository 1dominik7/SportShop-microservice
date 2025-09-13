import { useRef, useState } from "react";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import CurrencyExchangeIcon from "@mui/icons-material/CurrencyExchange";
import CalendarMonthIcon from "@mui/icons-material/CalendarMonth";
import GradeIcon from "@mui/icons-material/Grade";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/pagination";
import "swiper/css/autoplay";
import { Swiper, SwiperSlide, type SwiperRef } from "swiper/react";
import type { Swiper as SwiperType } from "swiper/types";
import { useProductsByFiltersGrouped } from "../../hooks/query";
import { useNavigate } from "react-router";
import LoadingAnimation from "../../ui/LoadingAnimation";
import { api } from "../../config/api";
import { emailValidationSchema } from "../../validator/userValidator";
import { toast, ToastContainer, ToastOptions } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

const producers = [
  {
    id: 1,
    name: "Nike",
    logo: "/nikeLogo.png",
  },
  {
    id: 2,
    name: "Adidas",
    logo: "/adidasLogo.png",
  },
  {
    id: 3,
    name: "Puma",
    logo: "/pumaLogo.png",
  },
  {
    id: 4,
    name: "New Balance",
    logo: "/newBalanceLogo.png",
  },
  {
    id: 5,
    name: "Under Armour",
    logo: "/underarmourLogo.png",
  },
];

const series = [
  {
    id: 1,
    name: "Nike Mercurial Vapor",
    logo: "/mercurialMain.jpg",
    variation: 37,
    variationOption: 112,
  },
  {
    id: 2,
    name: "Adidas Predator",
    logo: "/adidasPredatorMain.jpg",
    variation: 37,
    variationOption: 110,
  },
  {
    id: 3,
    name: "Puma Future",
    logo: "/pumaFutureMain.jpg",
    variation: 37,
    variationOption: 114,
  },
  {
    id: 4,
    name: "New Balance Tekela",
    logo: "/newBalanceMain.jpg",
    variation: 37,
    variationOption: 113,
  },
];

export const toastCustomize: ToastOptions = {
  position: "bottom-right",
  autoClose: 3000,
  hideProgressBar: false,
  closeOnClick: true,
  pauseOnHover: true,
};

const BannerCenter = () => {
  const swiperRef = useRef<SwiperType | null>(null);
  const swiperRefProducers = useRef<SwiperType | null>(null);
  const navigate = useNavigate();

  const [subscriber, setSubscriber] = useState<string>("");

  const { data, isLoading } = useProductsByFiltersGrouped(
    undefined,
    {},
    0,
    24,
    "createdDate",
    "desc",
    10
  );

  const goToNextSlide = () => {
    if (swiperRef.current) {
      swiperRef.current?.slideNext();
    }
  };

  const goToPrevSlide = () => {
    if (swiperRef.current) {
      swiperRef.current?.slidePrev();
    }
  };

  const goToNextSlideProducers = () => {
    if (swiperRefProducers.current) {
      swiperRef.current?.slideNext();
    }
  };

  const goToPrevSlideProducers = () => {
    if (swiperRefProducers.current) {
      swiperRef.current?.slidePrev();
    }
  };

  const subscribeHandler = async () => {
    try {
      await emailValidationSchema.validate({ email: subscriber });
      await api.post("/api/v1/newsletter/create", { email: subscriber });
      setSubscriber("");
      toast.success("Email added to newsletter successfully!", toastCustomize);
    } catch (error: any) {
      console.log(error);
      if (error.name === "ValidationError") {
        toast.error(error.message, toastCustomize);
      } else {
        toast.error(
          error?.response?.data || "Subscription failed.",
          toastCustomize
        );
      }
    }
  };

  return (
    <div className="flex flex-col justify-center items-center pt-12 space-y-16 max-lg:pt-6 max-lg:space-y-6 max-md:pt-2">
      <div className="w-[80%] flex flex-col space-y-12 max-lg:space-y-6">
        <div className="h-[25vh] flex flex-col space-y-8 max-lg:space-y-2 max-sm:h-[20vh]">
          <div className="flex h-full justify-around items-center py-4 max-lg:flex-wrap max-lg:py-0 max-sm:flex-col">
            <div className="flex items-center gap-2 max-lg:p-4 max-sm:p-0">
              <LocalShippingIcon />
              <span className="text-sm">Free delivery from 100$</span>
            </div>
            <div className="flex items-center gap-2 max-lg:p-4 max-sm:p-0">
              <CurrencyExchangeIcon />
              <span className="text-sm">Cashback system</span>
            </div>
            <div className="flex items-center gap-2 max-lg:p-4 max-sm:p-0">
              <CalendarMonthIcon />
              <span className="text-sm">30 days for free returns</span>
            </div>
            <div className="flex items-center gap-2 max-lg:p-4 max-sm:p-0">
              <GradeIcon />
              <span className="text-sm">User rating 4.9</span>
            </div>
          </div>
          <img
            className="h-max object-cover overflow-hidden max-sm:hidden"
            src="/mainpageimage.jpg"
            alt=""
          />
        </div>
      </div>
      <div className="relative h-full w-full flex flex-col items-center py-2 space-y-6">
        <div className="h-[40vh] w-[80%] flex gap-6 max-lg:h-[25vh] max-sm:flex-col">
          <img
            className="h-full w-full object-cover cursor-pointer"
            src="/mercurial.jpg"
            alt=""
            onClick={() => navigate(`/products?category=1&filters=37[112]`)}
          />
          <img
            className="h-full w-full object-cover cursor-pointer"
            src="/adidasPredator.jpg"
            alt=""
            onClick={() => navigate(`/products?category=1&filters=37[110]`)}
          />
        </div>
        <div className="absolute -top-14 left-0 w-full h-[300px] bg-gray-100 -z-10 max-lg:-top-8 max-md:-top-8 max-md:h-[200px]" />
        <div className="w-[80%] h-full space-y-6 max-lg:space-y-2">
          <div className="flex justify-between items-center">
            <span className="text-2xl font-bold">Newest</span>
            <div>
              <ChevronLeftIcon
                style={{ fontSize: 40, cursor: "pointer" }}
                onClick={goToPrevSlide}
              />
              <ChevronRightIcon
                style={{ fontSize: 40, cursor: "pointer" }}
                onClick={goToNextSlide}
              />
            </div>
          </div>
          <div className="relative w-full h-full">
            {isLoading ? (
              <div className="absolute top-0 left-0 flex w-full h-full justify-center items-center bg-white opacity-60 z-50">
                <LoadingAnimation />
              </div>
            ) : (
              <Swiper
                onSwiper={(swiper) => {
                  swiperRef.current = swiper;
                }}
                spaceBetween={30}
                breakpoints={{
                  0: {
                    slidesPerView: 2,
                  },
                  640: {
                    slidesPerView: 3,
                  },
                  768: {
                    slidesPerView: 4,
                  },
                  1024: {
                    slidesPerView: 5,
                  },
                }}
                loop={true}
                className="h-full object-cover"
              >
                {data?.content?.map((product, index) => (
                  <SwiperSlide
                    className="flex flex-col h-full group cursor-pointer overflow-hidden"
                    key={index}
                    onClick={() =>
                      navigate(
                        `products/${product.productId}${
                          product.colour !== "Unknown"
                            ? "-" + product?.colour
                            : ""
                        }`
                      )
                    }
                  >
                    <img
                      src={product?.productImages[0]?.imageFilename}
                      alt="Product 1"
                      className="h-[400px] w-full object-contain transform transition-transform duration-300 ease-in-out group-hover:scale-105 max-xl:h-[220px] max-sm:h-[190px]"
                    />
                    <span className="z-20 bg-white max-sm:max-w-[20ch] truncate max-sm:text-sm">
                      {product?.productName} - {product?.colour}
                    </span>
                    <div className="flex gap-3">
                      {product.productItemRequests.some(
                        (item) => item.discount > 0
                      ) ? (
                        <div className="flex gap-4 max-lg:gap-[2px] max-xl:flex-col max-xl:gap-[2px] max-sm:text-sm">
                          <span className="font-bold">
                            from{" "}
                            {Math.min(
                              ...product.productItemRequests.map(
                                (item) =>
                                  item.price -
                                  item.price * (item.discount / 100)
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
                            ...product.productItemRequests.map(
                              (item) => item.price
                            )
                          ).toFixed(2)}{" "}
                          $
                        </span>
                      )}
                    </div>
                  </SwiperSlide>
                ))}
              </Swiper>
            )}
          </div>
        </div>
      </div>
      <div className="relative h-full w-full flex flex-col items-center space-y-12">
        <div className="w-full h-[1px] bg-gray-200" />
        <div className="w-[80%] h-full space-y-12 py-3 max-md:space-y-6">
          <span className="text-3xl font-bold max-md:text-2xl">
            The most popular footwear collections
          </span>
          <div className="w-full h-full flex cursor-pointer">
            <Swiper
              spaceBetween={30}
              breakpoints={{
                640: {
                  slidesPerView: 1,
                },
                768: {
                  slidesPerView: 2,
                },
                1024: {
                  slidesPerView: 4,
                },
              }}
              loop={true}
              autoplay
              className="h-full object-cover"
            >
              {series?.map((item, index) => (
                <SwiperSlide
                  className="h-full flex flex-col items-center"
                  key={index}
                  onClick={() =>
                    navigate(
                      `/products?category=1&filters=${item?.variation}[${item?.variationOption}]`
                    )
                  }
                >
                  <img
                    src={item.logo}
                    alt={item.name}
                    className="h-[500px] object-cover max-md:h-[300px]"
                  />
                  <span className="font-bold text-2xl max-sm:text-xl">
                    {item?.name}
                  </span>
                </SwiperSlide>
              ))}
            </Swiper>
          </div>
        </div>
        <div className="absolute top-0 left-0 w-full h-[300px] bg-gray-100 -z-10" />
        <div className="w-[80%] h-full space-y-12 max-md:space-y-2">
          <div className="flex justify-between items-center">
            <span className="text-2xl font-bold">Producers</span>
            <div>
              <ChevronLeftIcon
                style={{ fontSize: 40, cursor: "pointer" }}
                onClick={goToPrevSlideProducers}
              />
              <ChevronRightIcon
                style={{ fontSize: 40, cursor: "pointer" }}
                onClick={goToNextSlideProducers}
              />
            </div>
          </div>
          <div className="w-full h-full">
            <Swiper
              onSwiper={(swiper) => {
                swiperRefProducers.current = swiper;
              }}
              spaceBetween={30}
              slidesPerView={1}
              breakpoints={{
                450: {
                  slidesPerView: 2,
                },
                768: {
                  slidesPerView: 3,
                },
                1024: {
                  slidesPerView: 4,
                },
              }}
              autoplay
              loop={true}
              className="flex items-center justify-center object-cover"
            >
              {producers?.map((item) => (
                <SwiperSlide
                  className="h-full flex items-center justify-center"
                  key={item.id}
                >
                  <img
                    src={item.logo}
                    alt={item.name}
                    className="h-[200px] w-full p-10 object-contain max-sm:w-[200px] max-sm:h-[150px] max-sm:p-4"
                  />
                </SwiperSlide>
              ))}
            </Swiper>
          </div>
        </div>
      </div>
      <div className="w-full h-full flex flex-col pt-4 bg-[rgb(77,77,84)] bg-gradient-to-r from-[rgb(77,77,84)] via-[rgb(23,23,24)] to-[rgb(101,104,105)]">
        <div className="flex flex-col items-center justify-center space-y-3 text-white max-md:px-4">
          <span className="font-bold text-3xl max-sm:text-2xl">Newsletter</span>
          <span className="max-sm:text-sm">
            Sign up for our newsletter and be the first to know about the latest
            promotions.
          </span>
          <div className="flex w-[600px] max-md:w-full px-4 py-5 h-[100px] max-sm:px-2">
            <input
              type="text"
              placeholder="Enter your e-mail"
              onChange={(e) => setSubscriber(e.target.value)}
              value={subscriber}
              className="w-[80%] p-4 outline-none text-black max-sm:text-sm max-sm:p-2"
            />
            <div className="flex items-center justify-center px-2 py-2 bg-white">
              <button
                className="bg-black py-3 px-6 text-white font-bold max-sm:px-2 max-sm:text-sm"
                onClick={subscribeHandler}
              >
                SUBSCRIBE
              </button>
            </div>
          </div>
        </div>
        <img
          src="/footerImage.jpg"
          alt=""
          className="h-[300px] object-cover object-bottom max-md:h-[200px]"
        />
      </div>
      <ToastContainer />
    </div>
  );
};

export default BannerCenter;
