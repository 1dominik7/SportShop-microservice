import  { useEffect, useState } from "react";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import { Link, useSearchParams } from "react-router";
import { api } from "../../config/api";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import CurrencyExchangeIcon from "@mui/icons-material/CurrencyExchange";
import CalendarMonthIcon from "@mui/icons-material/CalendarMonth";
import GradeIcon from "@mui/icons-material/Grade";

interface Props {
  numberOfProducts: number;
}

const Nav = ({ numberOfProducts }: Props) => {
  const [searchParams, setSearchParams] = useSearchParams();
  const categoryId = searchParams.get("category")
    ? parseInt(searchParams.get("category")!)
    : 0;
  const [categoryName, setCategoryName] = useState("");

  useEffect(() => {
    const getCategory = async () => {
      try {
        const res = await api.get(`/api/v1/category/byId?ids=${categoryId}`);
        setCategoryName(res.data[0].categoryName);
      } catch (error) {
        console.log(error);
      }
    };
    getCategory();
  }, [categoryId]);

  return (
    <>
      <div className="flex px-12 py-4 bg-gray-100 max-lg:px-6">
        <div className="flex items-end justify-center gap-2">
          <Link to='/' className="text-sm font-semibold">Home page</Link>
          <ChevronRightIcon style={{ fontSize: 16 }} />
          <span className="text-sm font-semibold text-grey-500 capitalize">{categoryName.length > 0 && categoryName}</span>
        </div>
      </div>
      <div className="px-12 max-md:px-6">
        <div className="flex items-center justify-between">
          <div className="flex flex-col gap-[1px] w-[100px]">
            <h1 className="w-max text-2xl font-semibold uppercase max-lg:text-xl">
              {categoryName.length > 0 && categoryName}
            </h1>
            <span className="font-semibold text-gray-400 w-full">
              {numberOfProducts > 0 ? numberOfProducts : 0} products
            </span>
          </div>
          <div className="flex justify-around items-center gap-8 max-md:hidden">
            <div className="flex items-center gap-2">
              <LocalShippingIcon />
              <span className="text-sm">Free delivery from 100$</span>
            </div>
            <div className="flex items-center gap-2">
              <CurrencyExchangeIcon />
              <span className="text-sm">Cashback system</span>
            </div>
            <div className="flex items-center gap-2">
              <CalendarMonthIcon />
              <span className="text-sm">30 days for free returns</span>
            </div>
            <div className="flex items-center gap-2">
              <GradeIcon />
              <span className="text-sm">user rating 4.9</span>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default Nav;
