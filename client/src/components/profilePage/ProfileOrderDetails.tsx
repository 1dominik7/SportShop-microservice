import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { api } from "../../config/api";
import {
  GetShopOrder,
  PaymentType,
  UserPaymentMethod,
} from "../../types/userTypes";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import LoadingAnimation from "../../ui/LoadingAnimation";

const ProfileOrderDetails = () => {
  const navigate = useNavigate();

  const { orderId } = useParams();

  const [order, setOrder] = useState<GetShopOrder | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [paymentIsLoading, setPaymentIsLoading] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [openPaymentWindow, setOpenPaymentWindow] = useState<boolean>(false);
  const [paymentType, setPaymentType] = useState<PaymentType[] | []>([]);

  useEffect(() => {
    const getOrder = async () => {
      setIsLoading(true);
      try {
        const res = await api.get(`/api/v1/shop-order/user/${orderId}`);
        setOrder(res.data);
      } catch (error) {
        console.error(error);
      } finally {
        setIsLoading(false);
      }
    };
    getOrder();
  }, [orderId]);

  const formatDate = (timestamp: string) => {
    const date = new Date(timestamp);
    return new Intl.DateTimeFormat("en-US", {
      weekday: "long",
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    }).format(date);
  };

  const handleTogglePaymentWindow = async () => {
  setOpenPaymentWindow(!openPaymentWindow);

  if (!openPaymentWindow && paymentType.length === 0) {
    try {
      setPaymentIsLoading(true);
      const res = await api.get("/api/v1/payment-type/all");
      setPaymentType(res.data);
    } catch (error) {
      console.error("Error fetching payment types:", error);
    } finally {
      setPaymentIsLoading(false);
    }
  }
};

  const getPaymentIcon = (methodName: string) => {
    const name = methodName.toLowerCase();
    if (name.includes("stripe")) return "stripe.png";
    if (name.includes("payu")) return "payu.png";
    return "payment.png";
  };

  const submitRetryPayment = async (value: string) => {
    if (!paymentType.some((type) => type.value === value)) {
      console.warn("Unsupported payment method: " + value);
    }

    setIsSubmitting(true);

    const request = {
      orderId,
      successUrl: `${window.location.origin}/payment-success`,
      cancelUrl: `${window.location.origin}/payment-cancel`,
    };
    try {
      const response = await api.post(
        `/api/v1/payment/${value}/pay-again`,
        request
      );
      window.location.href = response.data.checkoutUrl;
    } catch (error) {
      console.log("Error creating order: ", error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="py-11 px-20 max-md:px-6 max-sm:py-4">
      {isLoading ? (
        <div className="fixed top-0 left-0 flex h-full w-full z-50 bg-white opacity-80 items-center justify-center">
          <LoadingAnimation />
        </div>
      ) : (
        <div className="flex flex-col gap-2 overflow-y-scroll">
          <div className="flex items-center">
            <span
              className="cursor-pointer hover:underline"
              onClick={() => navigate("/profile/orders")}
            >
              Orders
            </span>
            <ChevronRightIcon />
            <span className="text-gray-400">{orderId}</span>
          </div>
          <div className="flex flex-col gap-2">
            <div className="flex justify-between">
              <div className="text-xl font-bold">
                <h1 className="text-3xl font-bold max-sm:text-xl">
                  Order Number: {orderId}
                </h1>
              </div>
              {order?.paymentId === null &&
                (isSubmitting ? (
                  <div className="flex items-center justify-center">
                    <LoadingAnimation height={50} width={50} />
                  </div>
                ) : (
                  <div className="relative">
                    <button
                      className="text-green-500 underline font-bold text-xl hover:opacity-55 cursor-pointer"
                      onClick={handleTogglePaymentWindow}
                    >
                      Pay for order
                    </button>
                    {openPaymentWindow && (
                      <div className="absolute bg-gray-100 top-7 left-0 flex flex-col px-9 border-[1px] border-black rounded-md">
                        {paymentIsLoading ? (
                          <LoadingAnimation height={50} width={50}/>
                        ) : (
                          paymentType?.map((type) => (
                            <div
                              key={type.id}
                              className="flex flex-col gap-[2px] items-center cursor-pointer hover:opacity-60"
                              onClick={() =>
                                submitRetryPayment(type.value.toLowerCase())
                              }
                            >
                              <img
                                src={`/${getPaymentIcon(type?.value)}`}
                                className="w-16 h-16 object-contain"
                                alt=""
                                loading="lazy"
                              />
                              {/* <span className="font-bold">{type?.value}</span> */}
                            </div>
                          ))
                        )}
                      </div>
                    )}
                  </div>
                ))}
            </div>
            <div className="flex gap-16 border-b-[1px] border-gray-300 pb-4 max-sm:flex-col max-sm:gap-2">
              <div className="flex flex-col max-sm:flex-row max-sm:justify-between max-sm:items-center">
                <span className="font-semibold">Order date:</span>
                <span>
                  {order?.orderDate ? formatDate(order.orderDate) : "-"}
                </span>
              </div>
              <div className="flex flex-col max-sm:flex-row max-sm:justify-between max-sm:items-center">
                <span className="font-semibold">Payment type:</span>
                <span>
                  {order?.paymentMethodName?.toUpperCase()
                    ? order?.paymentMethodName?.toUpperCase()
                    : "Not paid"}
                </span>
              </div>
                <div className="flex flex-col max-sm:flex-row max-sm:justify-between max-sm:items-center">
                <span className="font-semibold">Payment status:</span>
                <span>
                  {order?.paymentStatus
                    ? order?.paymentStatus
                    : "Not paid"}
                </span>
              </div>
            </div>
            <div className="flex flex-col gap-4 border-b-[1px] border-gray-300 pb-4 max-sm:gap-2">
              <span className="font-semibold">
                Delivery: {order?.shippingMethod?.name}
              </span>
              <div className="flex flex-col gap-6">
                {order?.orderLines?.map((item) => (
                  <div
                    key={`${order.id}-${item.id}`}
                    className="flex items-center justify-between cursor-pointer max-sm:flex-col max-sm:items-end max-sm:text-sm"
                    onClick={() => {
                      const colour = item?.productItem?.variationOptions?.find(
                        (v) => v?.variation?.name.toLowerCase() === "colour"
                      )?.value;

                      navigate(
                        `/products/${item?.productItem?.productId}-${colour}`
                      );
                    }}
                  >
                    <div className="flex gap-4">
                      <img
                        className="w-[100px] h-[150px] object-cover max-sm:h-[100px] max-sm:w-[50%] max-sm:object-contain"
                        src={item?.productItem?.productImages[0]?.imageFilename}
                        alt=""
                      />
                      <div className="flex flex-col justify-between max-sm:gap-2 max-sm:justify-normal">
                        <div className="flex flex-col">
                          <span className="capitalize font-semibold">
                            {item?.productItem?.variationOptions?.find(
                              (v) =>
                                v?.variation?.name.toLowerCase() === "brand"
                            )?.value ?? "-"}
                          </span>
                          <span className="font-semibold capitalize">
                            {item?.productItem?.productName}
                          </span>
                        </div>
                        <div className="flex flex-col text-gray-500">
                          {item?.productItem?.variationOptions?.find(
                            (v) => v?.variation?.name.toLowerCase() === "colour"
                          ) && (
                            <span>
                              {" "}
                              Colour:{" "}
                              {item?.productItem?.variationOptions?.find(
                                (v) =>
                                  v?.variation?.name.toLowerCase() === "colour"
                              )?.value ?? ""}
                            </span>
                          )}
                          {item?.productItem?.variationOptions?.find(
                            (v) => v?.variation?.name.toLowerCase() === "size"
                          ) && (
                            <span>
                              Size:{" "}
                              {item?.productItem?.variationOptions?.find(
                                (v) =>
                                  v?.variation?.name.toLowerCase() === "size"
                              )?.value ?? ""}
                            </span>
                          )}
                          <span>
                            Product code: {item?.productItem?.productCode}
                          </span>
                        </div>
                      </div>
                    </div>
                    <div className="flex flex-col items-end">
                      <span>Quantity: {item?.qty}</span>
                      <span className="font-bold">
                        {item?.price.toFixed(2)} $
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
            <div className="flex justify-between items-start py-4 border-b-[1px] border-gray-300 max-sm:flex-col max-sm:py-2">
              <h1 className="text-3xl font-bold max-sm:text-xl">Total cost</h1>
              <div className="w-[300px] flex justify-between flex-col max-sm:w-full">
                <div className="flex justify-between">
                  <span>Products price:</span>
                  <span>{order?.orderTotal.toFixed(2)} $</span>
                </div>
                {order?.appliedDiscountValue != undefined &&
                  order?.appliedDiscountValue > 0 && (
                    <div className="flex justify-between">
                      <span>Discount:</span>
                      <span>-{order.appliedDiscountValue} %</span>
                    </div>
                  )}
                <div className="flex justify-between">
                  <span>Shipping price</span>
                  <span>{order?.shippingMethod?.price} $</span>
                </div>
                <div className="flex justify-between mt-2 font-bold">
                  <span>Total cost:</span>
                  <span>{order?.finalOrderTotal.toFixed(2)} $</span>
                </div>
              </div>
            </div>
            <div className="flex flex-col gap-4 max-sm:gap-2">
              <h1 className="text-3xl font-bold max-sm:text-xl">
                Delivery address
              </h1>
              <div className="flex flex-col">
                <span>
                  {order?.shippingFirstName} {order?.shippingLastName}
                </span>
                <span>
                  {order?.shippingStreet} {order?.shippingAddressLine1}{" "}
                  {order?.shippingAddressLine2}
                </span>
                <span>
                  {order?.shippingPostalCode}, {order?.shippingCity} ,
                  {order?.shippingCountry}
                </span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProfileOrderDetails;
