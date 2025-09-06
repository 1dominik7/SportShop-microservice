import { Button, TextField } from "@mui/material";
import { useFormik } from "formik";
import { forgotPassword } from "../../state/authActions";
import { ForgotPasswordType } from "../../types/userTypes";
import { RotatingLines } from "react-loader-spinner";
import { AppDispatch, useAppSelector } from "../../state/store";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router";
import { forgotPasswordValidationSchema } from "../../validator/userValidator";

const props = {
  visible: true,
  height: "30",
  width: "30",
  color: "#6BE140",
  strokeWidth: "5",
  animationDuration: "0.75",
  ariaLabel: "rotating-lines-loading",
};

const ForgotPassword = () => {
  const navigate = useNavigate();
  const dispatch: AppDispatch = useDispatch();
  const loading = useAppSelector((store) => store.auth.loading);
  const error = useAppSelector((store) => store.auth.error);

  const formik = useFormik({
    initialValues: {
      email: "",
    },
    validationSchema: forgotPasswordValidationSchema,
    onSubmit: (values: ForgotPasswordType) => {
      dispatch(forgotPassword(values.email, navigate));
    },
  });

  return (
    <div className="w-full h-screen flex flex-col items-center justify-center space-y-6 max-md:px-6">
      <div className="flex flex-col h-max items-center space-y-4 justify-center px-12 py-24 border-[1px] border-gray-200 max-md:px-6 max-md:py-6">
        <h1 className="text-center font-bold text-3xl max-sm:text-2xl">Restart you password</h1>
        <span>Enter your email</span>
        <div className="w-[500px] space-y-5 max-md:w-full">
          <TextField
            fullWidth
            name="email"
            label="Email"
            value={formik.values.email}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            error={formik.touched.email && Boolean(formik.errors.email)}
            helperText={formik.touched.email && formik.errors.email}
          />
          <span
            className="text-blue-300 underline cursor-pointer"
            onClick={() => navigate("/login")}
          >
            Login
          </span>
          {error && (
            <div className="text-red-500 text-sm">
              {typeof error === "string" ? error : error?.message}
            </div>
          )}
          <Button
            onClick={() => formik.handleSubmit()}
            fullWidth
            variant="contained"
            sx={{ py: "11px", backgroundColor: "black", fontWeight: "bold" }}
            disabled={loading}
          >
            {loading ? <RotatingLines {...props} /> : "RESET PASSWORD"}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
