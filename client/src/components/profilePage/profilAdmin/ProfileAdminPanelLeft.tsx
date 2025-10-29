import { useLocation, useNavigate } from "react-router";

const navigation = [
  {
    id: 1,
    name: "products",
    title: "Products",
  },
  {
    id: 2,
    name: "addProduct",
    title: "Add Product",
  },
  {
    id: 3,
    name: "categories",
    title: "Categories",
  },
  {
    id: 4,
    name: "variations",
    title: "Variations",
  },
  {
    id: 5,
    name: "variationOptions",
    title: "Variation Options",
  },
  {
    id: 6,
    name: "discounts",
    title: "Discounts",
  },
  {
    id: 7,
    name: "orders",
    title: "Orders",
  },
  {
    id: 8,
    name: "users",
    title: "Users",
  },
  {
    id: 9,
    name: "statistics",
    title: "Statistics",
  },
   {
    id: 10,
    name: "shippingMethods",
    title: "Shipping Methods",
  },
   {
    id: 11,
    name: "orderStatuses",
    title: "Order Statuses",
  }
];

const ProfileAdminPanelLeft = () => {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <div className="h-full px-6 py-6 max-lg:px-4 max-lg:py-2">
      <div className="flex w-[320px] flex-col gap-4 font-bold text-lg text-center max-lg:w-full max-lg:gap-2">
        {navigation.map((item) => {
          const pathSegments = location.pathname.toLowerCase().split("/");
          const selectedOption = pathSegments.includes(item.name.toLowerCase());
          return (
            <div
              key={item.id}
              className={`py-2 px-6 hover:text-gray-500 cursor-pointer ${
                selectedOption ? "bg-gray-100 text-black" : "text-white"
              }`}
              onClick={() => navigate(item.name)}
            >
              {item.title}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default ProfileAdminPanelLeft;
