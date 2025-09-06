import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { api } from "../config/api";

const PaymentFailure = () => {
  const navigate = useNavigate();

  const [isPaymentValid, setIsPaymentValid] = useState<boolean | null>(null);

  const urlParams = new URLSearchParams(window.location.search);
  const paymentId = urlParams.get("payment_id");

  useEffect(() => {
    if (paymentId) {
      const verifyPayment = async () => {
        try {
          const res = await api.get(`/api/v1/payment/verify/${paymentId}`);
          if (res.data.status === "FAILED") {
            setIsPaymentValid(true);
          } else {
            setIsPaymentValid(false);
          }
        } catch (error) {
          console.error("Error verifying payment:", error);
          setIsPaymentValid(false);
        }
      };

      verifyPayment();
    } else {
      navigate("/");
    }
  }, [paymentId, navigate]);

  return (
    <div className="mt-[120px] max-md:mt-[100px]">
      <div className="flex flex-col pt-12 items-center gap-4 max-md:mt-6">
        <span className="font-bold text-3xl text-red-500 max-md:text-xl">Payment Failure</span>
        <span className="text-xl max-md:text-base">
          Check again or contact with our customer service
        </span>
        <button
          onClick={() => navigate("/")}
          className="text-white bg-black py-2 rounded font-bold px-5 cursor-pointer"
        >
          Go to the homepage
        </button>
      </div>
    </div>
  );
};

export default PaymentFailure;
