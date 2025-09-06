import { Outlet, useLocation, useNavigate } from "react-router";
import useIsAdmin from "../../hooks/useIsAdmin";
import { signOut } from "../../state/authSlice";
import { AppDispatch, useAppDispatch, useAppSelector } from "../../state/store";
import { useQueryClient } from "@tanstack/react-query";

const ProfileNavbar = () => {
  const dispatch: AppDispatch = useAppDispatch();
  const isAdmin = useIsAdmin();
  const navigate = useNavigate();
  const location = useLocation();
  const pathParts = location.pathname.split("/");
  const section = pathParts[2];
  const queryClient = useQueryClient();

  const formatSectionName = (section: string) => {
    const formatted = section
      .replace(/([A-Z])/g, " $1")
      .toLowerCase()
      .replace(/^./, (str) => str.toUpperCase());
    return formatted;
  };

  const logout = () => {
    dispatch(signOut());
    queryClient.removeQueries({ queryKey: ["userCart"] });
    navigate("/");
  };

  const { user } = useAppSelector((store) => store.auth);

  if (!user) return null;

  return (
    <div>
      <div className="py-11 px-20 bg-gray-200 max-md:lg-10 max-sm:px-4 max-md:py-5">
        <span className="font-bold text-3xl max-sm:text-2xl">
          {section && formatSectionName(section)}
        </span>
      </div>
      <div className="flex items-center gap-16 mt-6 px-20 border-b-[1px] border-gray-200 max-lg:px-10 max-lg:gap-6 max-sm:px-4 max-sm:gap-4 max-md:flex-wrap max-md:mt-0 max-md:py-4">
        <div
          className={`${
            section === "personalData"
              ? "border-b-2 border-secondary-color max-sm:border-none max-sm:text-secondary-color font-bold"
              : "border-b-2 border-transparent max-sm:border-none max-sm:text-black"
          } pb-6 px-6 cursor-pointer max-sm:px-2 max-sm:border-none max-sm:pb-2`}
          onClick={() => navigate("/profile/personalData")}
        >
          <span>Personal data</span>
        </div>
        <div
          className={`${
            section === "addresses"
              ? "border-b-2 border-secondary-color max-sm:border-none max-sm:text-secondary-color font-bold"
              : "border-b-2 border-transparent max-sm:border-none max-sm:text-black"
          } pb-6 px-6 cursor-pointer max-sm:px-2 max-sm:border-none max-sm:pb-2`}
          onClick={() => navigate("/profile/addresses")}
        >
          <span>Addresses</span>
        </div>
        <div
          className={`${
            section === "orders"
              ? "border-b-2 border-secondary-color max-sm:border-none max-sm:text-secondary-color font-bold"
              : "border-b-2 border-transparent max-sm:border-none max-sm:text-black"
          } pb-6 px-6 cursor-pointer max-sm:px-2 max-sm:border-none max-sm:pb-2`}
          onClick={() => navigate("/profile/orders")}
        >
          <span>Orders</span>
        </div>
        {isAdmin && (
          <div
            className={`${
              section === "adminPanel"
                ? "border-b-2 border-secondary-color max-sm:border-none max-sm:text-secondary-color font-bold"
                : "border-b-2 border-transparent max-sm:border-none max-sm:text-black"
            } pb-6 px-6 cursor-pointer max-sm:px-2 max-sm:border-none max-sm:pb-2`}
            onClick={() => navigate("/profile/adminPanel/products")}
          >
            <span>Admin panel</span>
          </div>
        )}
        <div className="text-red-500 pb-6 px-6 cursor-pointer max-sm:px-2 max-sm:pb-2">
          <span onClick={logout}>Logout</span>
        </div>
      </div>
      <Outlet />
    </div>
  );
};

export default ProfileNavbar;
