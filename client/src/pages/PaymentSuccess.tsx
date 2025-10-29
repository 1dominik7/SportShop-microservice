import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import Confetti from "react-confetti";
import { api } from "../config/api";
import { OrderDetails } from "../types/userTypes";
import LoadingAnimation from "../ui/LoadingAnimation";
import { useCartByUserId } from "../hooks/query";

const PaymentSuccess = () => {
  const navigate = useNavigate();
  const { refetch } = useCartByUserId();
  const [paymentSuccess, setPaymentSuccess] = useState(false);
  const [orderDetails, setOrderDetails] = useState<OrderDetails | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [windowDimensions, setWindowDimensions] = useState({
    width: window.innerWidth,
    height: window.innerHeight,
  });

  const urlParams = new URLSearchParams(window.location.search);
  const sessionId = urlParams.get("session_id");
  const orderId = urlParams.get("order_id");

  useEffect(() => {
    if (!sessionId) return;

    let interval: number;
    let attempts = 0;
    const maxAttempts = 5;

    const verifyStripePayment = async () => {
      try {
        const res = await api.get(`/api/v1/payment/verify/${sessionId}`);
        if (res.data.status === "SUCCEEDED") {
          clearInterval(interval);
          setPaymentSuccess(true);
          setOrderDetails(res.data);
          await refetch();
          setIsLoading(false);
        } else {
          attempts++;
          if (attempts >= maxAttempts) {
            clearInterval(interval);
            setIsLoading(false);
            setPaymentSuccess(false);
          }
        }
      } catch (error) {
        console.error("Error verifying Stripe payment:", error);
        attempts++;
        if (attempts >= maxAttempts) {
          clearInterval(interval);
          setIsLoading(false);
          setPaymentSuccess(false);
        }
      }
    };

    setIsLoading(true);
    interval = window.setInterval(verifyStripePayment, 3000);

    return () => clearInterval(interval);
  }, [navigate, sessionId]);

  useEffect(() => {
    if (!orderId) return;

    let interval: number;
    let attempts = 0;
    const maxAttempts = 5;

    setIsLoading(true);
    const verifyPayUPayment = async () => {
      try {
        const res = await api.get(`/api/v1/payment/verify/payu/${orderId}`);
        if (res.data.status === "SUCCEEDED") {
          setPaymentSuccess(true);
          setOrderDetails(res.data);
          await refetch();
          setIsLoading(false);
        } else {
          attempts++;
          if (attempts >= maxAttempts) {
            clearInterval(interval);
            setIsLoading(false);
            setPaymentSuccess(false);
          }
        }
      } catch (error) {
        console.error("Error verifying PayU payment:", error);
        attempts++;
        if (attempts >= maxAttempts) {
          clearInterval(interval);
          setIsLoading(false);
          setPaymentSuccess(false);
        }
      }
    };
    interval = window.setInterval(verifyPayUPayment, 3000);

    return () => clearInterval(interval);
  }, [navigate, orderId, sessionId]);

  useEffect(() => {
    if (!sessionId && !orderId) {
      setPaymentSuccess(false);
      navigate("/");
    }
  }, [navigate, orderId, sessionId]);

  useEffect(() => {
    const handleResize = () => {
      setWindowDimensions({
        width: window.innerWidth,
        height: window.innerHeight,
      });
    };

    window.addEventListener("resize", handleResize);
    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  return (
    <div className="mt-[120px]">
      {isLoading ? (
        <div className="absolute top-0 left-0 flex w-full h-full justify-center items-center bg-white opacity-60 z-50">
          <LoadingAnimation />
        </div>
      ) : (
        <>
          {paymentSuccess ? (
            <>
              <Confetti
                width={windowDimensions.width}
                height={windowDimensions.height}
                recycle={false}
                numberOfPieces={300}
                gravity={0.3}
                colors={["#ff0000", "#ff9900", "#33cc33", "#3399ff", "#ff0099"]}
              />
              <div className="flex flex-col pt-12 items-center max-md:pt-6 max-md:px-8">
                <div className="max-w-[700px] flex flex-col items-center gap-2 border-[1px] border-gray-200 px-12 py-6 rounded-xl bg-gray-100 max-md:w-full max-md:px-4">
                  <span className="font-bold text-3xl text-secondary-color max-md:text-xl max-md: text-center">
                    Payment Scuccess
                  </span>
                  <p className="font-bold">
                    Order number: {orderDetails?.shopOrder?.id}
                  </p>
                  <div className="flex flex-col gap-2 mt-2">
                    {orderDetails?.shopOrder?.orderLines?.map((order) => (
                      <div
                        key={order?.id}
                        className="flex gap-4 justify-between items-center border-b-[1px] border-gray-200 pb-2 max-md:text-sm"
                      >
                        <span className="font-bold">{order?.productName}</span>
                        <span className="max-md:w-[80px]">
                          {order?.qty} qty.
                        </span>
                        <span className="font-bold max-md:w-[80px]">
                          {order?.price?.toFixed(2)} $
                        </span>
                      </div>
                    ))}
                  </div>
                  <div className="flex items-center gap-2 border-b-[1px] border-gray-200 pb-2 max-md:text-sm">
                    <span className="font-bold">Shipping Method:</span>
                    <span>{orderDetails?.shopOrder?.shippingMethod?.name}</span>
                    <span>
                      {orderDetails?.shopOrder?.shippingMethod?.price} $
                    </span>
                  </div>
                  {orderDetails?.shopOrder?.appliedDiscountValue !==
                    undefined &&
                    orderDetails?.shopOrder?.appliedDiscountValue > 0 && (
                      <div className="flex items-center gap-2">
                        <span>Discount:</span>
                        <span>
                          {orderDetails?.shopOrder?.appliedDiscountValue} %
                        </span>
                      </div>
                    )}
                  <p className="font-bold">
                    Total amount:{" "}
                    {orderDetails?.shopOrder?.finalOrderTotal.toFixed(2)} $
                  </p>
                  <button
                    onClick={() => navigate("/")}
                    className="text-white bg-black py-2 rounded font-bold px-5 cursor-pointer max-md:mt-2"
                  >
                    Go to the homepage
                  </button>
                </div>
              </div>
            </>
          ) : (
            <div className="flex flex-col pt-12 items-center gap-4">
              <span className="font-bold text-3xl text-red-500">
                Payment Failure
              </span>
              <span className="text-xl">
                Check again or contact with our customer service
              </span>
              <button
                onClick={() => navigate("/")}
                className="text-white bg-black py-2 rounded font-bold px-5 cursor-pointer"
              >
                Go to the homepage
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default PaymentSuccess;
