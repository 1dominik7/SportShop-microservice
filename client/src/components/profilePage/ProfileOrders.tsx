import { useUserShopOrders } from "../../hooks/query";
import LoadingAnimation from "../../ui/LoadingAnimation";
import { useNavigate } from "react-router";

function ProfileOrders() {
  const navigate = useNavigate();

  const { data, isLoading } = useUserShopOrders();

  const formatDate = (timestamp: string) => {
    const date = new Date(timestamp);
    return new Intl.DateTimeFormat("en-US", {
      weekday: "long",
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    }).format(date);
  };

  return (
  <div className="relative py-11 px-20 max-md:px-6 max-sm:py-6 min-h-[calc(100vh-320px)]">
      {isLoading ? (
        <div className="absolute top-0 left-0 flex h-full w-full z-50 bg-white opacity-80 items-center justify-center">
          <LoadingAnimation />
        </div>
      ) : (
        <div className="flex flex-col gap-6">
          {data?.map((order) => (
            <div
              key={order.id}
              className="flex flex-col gap-4 border-b-[1px] border-gray-300 pb-4 max-md:gap-2"
            >
              <div className="flex justify-between items-center gap-2 font-bold">
                <span>Order number: {order.id}</span>
                <span
                  className="text-secondary-color cursor-pointer border-b-2 border-b-transparent hover:border-b-secondary-color"
                  onClick={() => navigate(`${order.id}`)}
                >
                  Check order
                </span>
              </div>
              <div className="flex gap-12 max-md:flex-col max-md:gap-2">
                <div className="flex flex-col">
                  <span className="font-semibold">Order Date:</span>
                  <span>{formatDate(order?.orderDate)}</span>
                </div>
                <div className="flex flex-col">
                  <span className="font-semibold">Total price:</span>
                  <span>{order?.finalOrderTotal} $</span>
                </div>
                <div className="flex flex-col">
                  <span className="font-semibold">Payment Status:</span>
                  <span className="capitalize">
                    {order?.orderStatus?.status}
                  </span>
                </div>
              </div>
              <div className="flex flex-grow gap-6 max-sm:flex-col">
                {order?.orderLines?.map((item) => (
                  <div
                    key={`${item.id}-${item?.productItem?.id}`}
                    className="w-[200px] cursor-pointer max-sm:w-full max-sm:flex max-sm:gap-2 max-sm:text-sm"
                    onClick={() => {
                      const colour = item?.productItem?.variationOptions?.find(
                        (v) => v?.variation?.name.toLowerCase() === "colour"
                      )?.value;

                      navigate(
                        `/products/${item?.productItem?.productId}-${colour}`
                      );
                    }}
                  >
                    <img
                      className="h-[300px] object-cover max-sm:h-[100px] max-sm:w-[50%] max-sm:object-contain"
                      src={item?.productItem?.productImages[0]?.imageFilename}
                      alt=""
                      loading="lazy"
                    />
                    <span className="font-semibold max-sm:w-full">{item?.productName}</span>
                  </div>
                ))}
              </div>
              <button onClick={() => navigate(`review/${order.id}`)} className="bg-black text-white p-4 font-bold text-xl max-md:text-sm max-md:p-3">Add Review</button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default ProfileOrders;
