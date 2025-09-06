import { Input } from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import PersonOutlineIcon from "@mui/icons-material/PersonOutline";
import FavoriteBorderIcon from "@mui/icons-material/FavoriteBorder";
import ShoppingBagOutlinedIcon from "@mui/icons-material/ShoppingBagOutlined";
import { useLocation, useNavigate } from "react-router";
import { AppDispatch, useAppDispatch, useAppSelector } from "../state/store";
import { useEffect, useState } from "react";
import {
  useCartByUserId,
  useCategoryQuery,
  useFavorites,
} from "../hooks/query";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import CartSlider from "./cart/CartSlider";
import MenuIcon from "@mui/icons-material/Menu";
import CloseIcon from "@mui/icons-material/Close";
import { useQueryClient } from "@tanstack/react-query";
import { signOut } from "../state/authActions";

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch: AppDispatch = useAppDispatch();
  const queryClient = useQueryClient();

  const [scrolled, setScrolled] = useState(false);
  const [hoverCategoryId, setHoverCategoryId] = useState<number | null>(null);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [openMobileMenu, setOpenMobileMenu] = useState(false);

  const isHomePage = location.pathname === "/";
  const isLoggedIn = useAppSelector((store) => store?.auth?.isLoggedIn);

  const { data: cart } = useCartByUserId();
  const { data } = useCategoryQuery([1, 2, 3, 4, 5, 6, 7]);

  const logout = () => {
    dispatch(signOut());
    setIsCartOpen(false);
    queryClient.removeQueries({ queryKey: ["userCart"] });
    navigate("/");
  };

  useEffect(() => {
    const handleScroll = () => {
      if (window.scrollY > 50) {
        setScrolled(true);
      } else {
        setScrolled(false);
      }
    };

    window.addEventListener("scroll", handleScroll);

    return () => {
      window.removeEventListener("scroll", handleScroll);
    };
  }, []);

  const toggleCart = () => {
    setIsCartOpen(!isCartOpen);
  };

  useEffect(() => {
    if (!isLoggedIn) {
      queryClient.removeQueries({ queryKey: ["userCart"] });
    }
  }, [isLoggedIn, queryClient]);

  const handleFilterClick = (
    categoryId: number,
    variationId: number,
    optionIds: string
  ) => {
    const searchParams = new URLSearchParams(location.search);
    searchParams.set("category", categoryId.toString());
    searchParams.set("filters", `${variationId}[${optionIds}]`);
    navigate(`/products?${searchParams.toString()}`, { replace: true });
  };

  const handleCategoryClick = (categoryId: number) => {
    const searchParams = new URLSearchParams(location.search);
    searchParams.delete("filters");

    searchParams.set("category", categoryId.toString());

    const newUrl = `/products?${searchParams.toString()}`;

    navigate(newUrl, { replace: true });
  };

  const { data: favorites } = useFavorites();

  return (
    <div className="flex flex-col relative">
      <div
        className={`h-16 fixed w-full flex flex-col top-0 left-0 z-40 group ${
          scrolled || !isHomePage ? "bg-black" : ""
        }`}
      >
        <div className="flex justify-between items-center px-8 bg-transparent w-full group-hover:bg-black transition-all duration-500 ease-in-out z-20 max-md:px-4">
          <div
            className="flex gap-4 items-center cursor-pointer"
            onClick={() => navigate("/")}
          >
            <img
              src="/shop-logo.png"
              alt="Shop Logo"
              className="w-16 h-16 object-cover bg-none"
            />
            <span className="text-xl font-bold text-white max-md:hidden">
              Sport Shop
            </span>
          </div>
          <div className="flex items-center text-white space-x-12 max-lg:space-x-6">
            <div className="relative border-b-[1px] border-white p-[1px] max-md:hidden">
              <Input
                type="text"
                className="text-white padding-r-5"
                sx={{ color: "white", paddingRight: 5 }}
                placeholder="Search...."
              />
              <SearchIcon
                sx={{
                  fontSize: 26,
                  cursor: "pointer",
                  position: "absolute",
                  right: 0,
                  top: 4,
                  "&:hover": {
                    color: "#6BE140",
                  },
                }}
              />
            </div>
            {isLoggedIn ? (
              <PersonOutlineIcon
                sx={{
                  fontSize: 26,
                  cursor: "pointer",
                  "&:hover": {
                    color: "#6BE140",
                  },
                }}
                onClick={() => navigate("/profile/personalData")}
              />
            ) : (
              <PersonOutlineIcon
                sx={{
                  fontSize: 26,
                  cursor: "pointer",
                  "&:hover": {
                    color: "#6BE140",
                  },
                }}
                onClick={() => navigate("/login")}
              />
            )}
            <div className="relative" onClick={() => navigate("/favorite")}>
              <FavoriteBorderIcon
                sx={{
                  fontSize: 26,
                  cursor: "pointer",
                  "&:hover": {
                    color: "#6BE140",
                  },
                }}
              />
              {favorites && favorites?.length > 0 && (
                <div className="absolute -top-3 -right-3 bg-secondary-color w-5 h-5 rounded-full flex items-center justify-center">
                  <span className="text-sm">{favorites?.length}</span>
                </div>
              )}
            </div>
            <div className="relative">
              {cart && cart?.shoppingCartItems?.length > 0 && (
                <div className="absolute -top-3 -right-3 bg-secondary-color w-5 h-5 rounded-full flex items-center justify-center">
                  <span className="text-sm">
                    {cart.shoppingCartItems.length}
                  </span>
                </div>
              )}
              <ShoppingBagOutlinedIcon
                sx={{
                  fontSize: 26,
                  cursor: "pointer",
                  "&:hover": {
                    color: "#6BE140",
                  },
                }}
                onClick={toggleCart}
              />
            </div>
            {isLoggedIn && (
              <div className="cursor-pointer max-lg:hidden" onClick={logout}>
                Logout
              </div>
            )}
            <div
              className="lg:hidden cursor-pointer"
              onClick={() => setOpenMobileMenu(!openMobileMenu)}
            >
              <MenuIcon sx={{ fontSize: 26 }} />
            </div>
          </div>
        </div>
        <div
          className={`flex justify-center items-center gap-6 bg-transparent text-white group-hover:bg-white h-auto ${
            scrolled ? "bg-white text-black" : "bg-transparent"
          } ${
            !isHomePage && "border-b-[1px] border-b-gray-100"
          } group-hover:text-black transition-all duration-500 ease-in-out max-lg:hidden`}
        >
          {data?.map((category) => (
            <div
              className="flex h-14 mx-6 items-center cursor-pointer z-90"
              key={category.id}
              onMouseEnter={() => setHoverCategoryId(category.id ?? null)}
              onMouseLeave={() => setHoverCategoryId(null)}
            >
              <button
                onClick={() => handleCategoryClick(category.id ?? 0)}
                className={`uppercase font-bold hover:text-secondary-color ${
                  scrolled || !isHomePage
                    ? "text-black"
                    : "text-white group-hover:text-black"
                }`}
              >
                {category?.categoryName}
              </button>
              {hoverCategoryId === category.id && category.variations && (
                <div
                  className={`absolute opacity-0 top-[120px] left-0 w-full p-6 flex justify-between h-auto bg-white transition-all duration-900 delay-800 ease-out ${
                    hoverCategoryId === category.id
                      ? "opacity-100 translate-y-0"
                      : "opacity-0 translate-y-4"
                  }`}
                >
                  <div className="w-full flex flex-wrap justify-start gap-24 items-start px-4 max-lg:gap-12">
                    {category.variations.map((variation) => (
                      <div
                        key={variation.id}
                        className="text-black flex flex-col flex-wrap gap-[6px]"
                      >
                        <span
                          onClick={(e) => {
                            e.stopPropagation();
                            handleFilterClick(
                              category.id ?? 0,
                              variation.id,
                              variation.options
                                .map((option) => option.id.toString())
                                .join("%")
                            );
                          }}
                          className="font-bold text-lg hover:text-secondary-color capitalize"
                        >
                          {variation.name}
                        </span>
                        {variation?.options
                          ?.slice()
                          .sort((a, b) => {
                            if (variation.name.toLowerCase() === "size") {
                              const sizeOrder = [
                                "xs",
                                "s",
                                "m",
                                "l",
                                "xl",
                                "xxl",
                              ];
                              const aValue = a.value.toLowerCase();
                              const bValue = b.value.toLowerCase();

                              const aInOrder = sizeOrder.includes(aValue);
                              const bInOrder = sizeOrder.includes(bValue);

                              if (aInOrder && bInOrder) {
                                return (
                                  sizeOrder.indexOf(aValue) -
                                  sizeOrder.indexOf(bValue)
                                );
                              }

                              if (aInOrder) return -1;
                              if (bInOrder) return 1;

                              const aNum = parseFloat(aValue);
                              const bNum = parseFloat(bValue);

                              if (!isNaN(aNum) && !isNaN(bNum)) {
                                return parseFloat(aValue) - parseFloat(bValue);
                              }

                              return aValue.localeCompare(bValue);
                            }
                            return a.value.localeCompare(b.value);
                          })
                          .map((option, index) => (
                            <div
                              key={index}
                              onClick={() =>
                                navigate(
                                  `/products?category=${category.id}&filters=${variation.id}[${option.id}]`
                                )
                              }
                              className="hover:text-secondary-color capitalize"
                            >
                              {option.value}
                            </div>
                          ))}
                      </div>
                    ))}
                  </div>
                  <div className="relative flex items-center justify-center h-72 w-72">
                    <img
                      src="/shoesnavbar.png"
                      alt=""
                      className="object-cover w-72 h-72"
                    />
                    <div className="absolute bottom-2 left-0 flex p-4 gap-4 bg-white text-black">
                      <button
                        className="hover:text-secondary-color"
                        onClick={() =>
                          navigate(`/products?category=1&filters=4[5]`)
                        }
                      >
                        Nike Murcurial Vapor
                      </button>
                      <ChevronRightIcon />
                    </div>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
        {openMobileMenu && (
          <div className="fixed top-0 left-0 w-full h-screen bg-white z-40 py-6 px-4 overflow-y-auto lg:hidden transition-all duration-500">
            <div
              onClick={() => setOpenMobileMenu(!openMobileMenu)}
              className="flex justify-between items-center mb-6"
            >
              <span className="text-xl font-bold">Categories</span>
              <CloseIcon sx={{ fontSize: 36, cursor: "pointer" }} />
            </div>
            {data?.map((category) => (
              <div key={category.id} className="mb-8">
                <div className="flex justify-between items-center border-b-[1px] border-gray-200">
                  <button
                    onClick={() => {
                      handleCategoryClick(category.id ?? 0);
                      setOpenMobileMenu(false);
                    }}
                    className="uppercase text-black mb-2 block w-full text-left text-xl"
                  >
                    {category.categoryName}
                  </button>
                  <ChevronRightIcon sx={{ fontSize: 36 }} />
                </div>
              </div>
            ))}
            {isLoggedIn && (
              <button
                className="w-full mt-12 font-bold underline text-3xl flex justify-center"
                onClick={logout}
              >
                Logout
              </button>
            )}
          </div>
        )}
      </div>
      <CartSlider isOpen={isCartOpen} onClose={toggleCart} />
    </div>
  );
};

export default Navbar;
