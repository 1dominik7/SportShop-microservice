import Basket from "../components/basketPage/Basket";
import Delivery from "../components/basketPage/Delivery";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import CalendarMonthIcon from "@mui/icons-material/CalendarMonth";
import CurrencyExchangeIcon from "@mui/icons-material/CurrencyExchange";
import GradeIcon from "@mui/icons-material/Grade";
import ShoppingBagOutlinedIcon from "@mui/icons-material/ShoppingBagOutlined";
import FactCheckOutlinedIcon from "@mui/icons-material/FactCheckOutlined";
import { useLocation, useNavigate } from "react-router";

const BasketPage = () => {
  const navigate = useNavigate();

  const { pathname } = useLocation();

  return (
    <div className="w-full h-full mt-[120px] max-lg:mt-[100px]">
      <div className="flex justify-around items-center py-4 bg-gray-100 max-md:px-2">
        <div className="flex items-center gap-2 max-md:flex-col-reverse max-md:text-center">
          <LocalShippingIcon />
          <span className="text-sm max-md:text-xs">
            Free delivery from 100$
          </span>
        </div>
        <div className="flex items-center gap-2 max-md:flex-col-reverse max-md:text-center">
          <CurrencyExchangeIcon />
          <span className="text-sm max-md:text-xs">Cashback system</span>
        </div>
        <div className="flex items-center gap-2 max-md:flex-col-reverse max-md:text-center">
          <CalendarMonthIcon />
          <span className="text-sm max-md:text-xs">
            30 days for free returns
          </span>
        </div>
        <div className="flex items-center gap-2 max-md:flex-col-reverse max-md:text-center">
          <GradeIcon />
          <span className="text-sm max-md:text-xs">User rating 4.9</span>
        </div>
      </div>
      <div className="flex w-full items-center gap-4 py-6 px-16 border-b-[1px] border-b-gray-100 max-md:px-4 max-md:py-4 max-md:items-start">
        <div className="w-[220px] flex items-center gap-2 max-md:flex-col max-md:w-full cursor-pointer">
          <ShoppingBagOutlinedIcon
            onClick={() => navigate("/basket")}
            sx={{
              fontSize: {
                xs: 36,
                sm: 46,
              },
            }}
            className={`p-2 border-[1px] rounded-full ${
              pathname === "/basket"
                ? "text-secondary-color border-secondary-color"
                : "text-black border-black"
            }`}
          />
          <span className="text-lg font-semibold max-md:text-base full">
            1. Basket
          </span>
        </div>
        <span className="w-full h-[1px] flex-grow bg-gray-100 max-md:mt-6"></span>
        <div
          className="w-[500px] flex items-center gap-2 max-md:flex-col max-md:w-full cursor-pointer"
          onClick={() => navigate("/basket/delivery")}
        >
          <FactCheckOutlinedIcon
            sx={{
              fontSize: {
                xs: 36,
                sm: 46,
              },
            }}
            className={`p-2 border-[1px] rounded-full ${
              pathname === "/basket/delivery"
                ? "text-secondary-color border-secondary-color"
                : "text-black border-black"
            }`}
          />
          <span className="text-lg font-semibold max-md:text-base">
            2. Delivery and payment
          </span>
        </div>
      </div>
      {pathname === "/basket" ? <Basket /> : <Delivery />}
    </div>
  );
};

export default BasketPage;
