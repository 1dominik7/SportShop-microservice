import React from "react";
import { useNavigate } from "react-router";

const NotFound = () => {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col mt-[120px] pt-12 items-center gap-4">
      <div className="flex items-center gap-2">
        <div className="h-4 w-4 bg-secondary-color rounded-full leading-10"></div>
        <span className="font-bold text-3xl">Page not found</span>
      </div>
      <div></div>
      <button
        onClick={() => navigate("/")}
        className="text-white bg-black py-2 rounded font-bold px-5 cursor-pointer"
      >
        Go to the homepage
      </button>
    </div>
  );
};

export default NotFound;
