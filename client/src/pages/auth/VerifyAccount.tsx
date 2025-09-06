import { Button } from "@mui/material";
import { useEffect, useState } from "react";
import ReactCodeInput from "react-code-input";
import { useDispatch, useSelector } from "react-redux";
import { useLocation, useNavigate } from "react-router";
import { verifyAccount } from "../../state/authActions";
import { AppDispatch, RootState } from "../../state/store";
import { RotatingLines } from "react-loader-spinner";
import { clearError } from "../../state/authSlice";

const props = {
  visible: true,
  height: "30",
  width: "30",
  color: "#6BE140",
  strokeWidth: "5",
  animationDuration: "0.75",
  ariaLabel: "rotating-lines-loading",
};

const VerifyAccount = () => {
  const navigate = useNavigate();
  const dispatch: AppDispatch = useDispatch();
  const [isPinCodeValid, setIsPinCodeValid] = useState<boolean>(true);
  const [code, setCode] = useState<string>("");
  const error = useSelector((state: RootState) => state.auth.error);
  const loading = useSelector((state: RootState) => state.auth.loading);
  const location = useLocation();
  const email = location.state?.email || "";

  const handleChange = (newCode: string) => {
    setCode(newCode);
  };

  const checkCode = () => {
    if (code) {
      dispatch(verifyAccount(code, navigate));
    } else {
      alert("Please enter the verification code.");
    }
  };

  useEffect(() => {
    dispatch(clearError());
  }, [dispatch]);

  return (
    <div className="w-full h-screen flex flex-col items-center justify-center space-y-6">
      <h1 className="text-center font-bold text-3xl">
        Enter your code to verify account
      </h1>
      <span>
        To log in, enter the 6-digit code that we sent to {email}.
      </span>
      <div className="h-1/2 flex flex-col space-y-6 items-center">
        <ReactCodeInput
          type="text"
          value={code}
          onChange={handleChange}
          isValid={isPinCodeValid}
          fields={6}
          name="verification-code"
          inputMode="numeric"
          inputStyle={{
            width: "60px",
            height: "60px",
            fontSize: "32px",
            textAlign: "center",
            borderRadius: "8px",
            borderWidth: "2px",
            borderColor: "black",
            color: "black",
            margin: "0 5px",
          }}
        />
        {error && (
          <div className="text-sm text-red-500">
            {typeof error === "string" ? error : error?.message}
          </div>
        )}
        <Button
          fullWidth
          variant="contained"
          sx={{ py: "11px", backgroundColor: "black", fontWeight: "bold" }}
          onClick={checkCode}
          disabled={loading}
        >
          {loading ? <RotatingLines {...props} /> : "Verify"}
        </Button>
        <span
          onClick={() => navigate("/login")}
          className="text-center font-bold underline text-secondary-color cursor-pointer"
        >
          Sign In to another account
        </span>
      </div>
    </div>
  );
};

export default VerifyAccount;
