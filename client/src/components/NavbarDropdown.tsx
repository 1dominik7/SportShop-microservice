import React from "react";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import { Category } from "../types/userTypes";

interface NavbarDropdownProps {
  category: Category;
  hoverCategoryId: number | null;
}

const NavbarDropdown: React.FC<NavbarDropdownProps> = ({
  category,
  hoverCategoryId,
}) => {

  return (
    <div
      className={`absolute top-[120px] left-0 w-full p-6 flex justify-between h-auto bg-white transition-all duration-500 ease-out ${
        hoverCategoryId === category.id
          ? "opacity-100 translate-y-0"
          : "opacity-0 translate-y-4"
      }`}
    >
      {category?.variations?.map((variation) => (
        <div
          key={variation.id}
          className="text-black flex flex-col flex-wrap gap-[6px]"
        >
          <span className="font-bold text-lg mb-2 hover:text-secondary-color">
            {variation.name}
          </span>
          {variation?.options?.map((option, index) => (
            <span key={index} className="hover:text-secondary-color">
              {option.value}
            </span>
          ))}
        </div>
      ))}
      <div className="relative flex items-center justify-center">
        <img src="/shoesnavbar.png" alt="" className="object-cover w-72 h-72" />
        <div className="absolute bottom-2 left-0 flex p-4 gap-4 bg-white text-black">
          <span className="hover:text-secondary-color">
            Nike Murcurial Vapor
          </span>
          <ChevronRightIcon />
        </div>
      </div>
    </div>
  );
};

export default NavbarDropdown;
