import { Button, TextField } from "@mui/material";
import { useFormik } from "formik";
import { AppDispatch, useAppSelector } from "../../state/store";
import { useDispatch } from "react-redux";
import { SignInPayload } from "../../types/userTypes";
import { loginValidationSchema } from "../../validator/userValidator";
import { signIn } from "../../state/authActions";
import { useNavigate } from "react-router";
import { RotatingLines } from "react-loader-spinner";
import { useEffect } from "react";
import { clearError } from "../../state/authSlice";
import { ToastContainer, ToastOptions } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

const props = {
  visible: true,
  height: "30",
  width: "30",
  color: "#6BE140",
  strokeWidth: "5",
  animationDuration: "0.75",
  ariaLabel: "rotating-lines-loading",
};

export const toastCustomize: ToastOptions = {
  position: "bottom-right",
  autoClose: 3000,
  hideProgressBar: false,
  closeOnClick: true,
  pauseOnHover: true,
};

const Login = () => {
  const navigate = useNavigate();
  const dispatch: AppDispatch = useDispatch();
  const loading = useAppSelector((store) => store.auth.loading);
  const error = useAppSelector((store) => store.auth.error);
  const isLoggedIn = useAppSelector((store) => store.auth.isLoggedIn);

  const formik = useFormik({
    initialValues: {
      email: "",
      password: "",
    },
    validationSchema: loginValidationSchema,
    onSubmit: (values: SignInPayload) => {
      dispatch(signIn(values, navigate));
    },
  });

  useEffect(() => {
    dispatch(clearError());
  }, [dispatch]);

  useEffect(() => {
    if (isLoggedIn) {
      navigate("/");
    }
  }, [isLoggedIn, navigate]);

  return (
    <div className="h-screen flex items-center justify-center max-md:px-6">
      <div className="flex flex-col h-max items-center justify-center px-12 py-24 border-[1px] border-gray-200 max-md:px-6 max-md:py-6">
        <h1 className="text-center font-bold text-3xl pb-8 max-sm:text-2xl">LOGIN</h1>
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
          <TextField
            fullWidth
            name="password"
            label="Password"
            type="password"
            value={formik.values.password}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            error={formik.touched.password && Boolean(formik.errors.password)}
            helperText={formik.touched.password && formik.errors?.password}
          />
          <span
            className="text-blue-300 underline cursor-pointer"
            onClick={() => navigate("/forgot-password")}
          >
            Forgot password?
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
            {loading ? <RotatingLines {...props} /> : "Sign In"}
          </Button>
          <span>
            Don't have an account?{" "}
            <span
              className="underline text-blue-300 cursor-pointer"
              onClick={() => navigate("/register")}
            >
              Register
            </span>
          </span>
        </div>
        <ToastContainer />
      </div>
    </div>
  );
};

export default Login;
