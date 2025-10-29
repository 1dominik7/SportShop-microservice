import { Button, TextField } from "@mui/material";
import { useFormik } from "formik";
import { useDispatch } from "react-redux";
import { signUp } from "../../state/authActions";
import { SignUpPayload } from "../../types/userTypes";
import { AppDispatch, useAppSelector } from "../../state/store";
import { registerValidationSchema } from "../../validator/userValidator";
import { useNavigate } from "react-router";
import { RotatingLines } from "react-loader-spinner";
import { useEffect } from "react";
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

const Register = () => {
  const navigate = useNavigate();
  const dispatch: AppDispatch = useDispatch();

  const error = useAppSelector((store) => store.auth.error);
  const loading = useAppSelector((store) => store.auth.loading);

  const formik = useFormik({
    initialValues: {
      email: "",
      firstname: "",
      lastname: "",
      password: "",
    },
    validationSchema: registerValidationSchema,
    onSubmit: (values: SignUpPayload) => {
      dispatch(signUp(values, navigate));
    },
  });

  useEffect(() => {
    dispatch(clearError());
  }, [dispatch]);

  return (
    <div className="h-screen flex items-center justify-center max-md:px-6">
      <div className="flex flex-col h-max items-center justify-center px-12 py-24 border-[1px] border-gray-200 max-md:px-6 max-md:py-6">
        <h1 className="text-center font-bold text-3xl pb-8 max-sm:text-2xl">
          REGISTER
        </h1>
        <form
          onSubmit={formik.handleSubmit}
          className="w-[500px] space-y-5 max-md:w-full"
        >
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
            name="firstname"
            label="First Name"
            value={formik.values.firstname}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            error={formik.touched.firstname && Boolean(formik.errors.firstname)}
            helperText={formik.touched.firstname && formik.errors?.firstname}
          />
          <TextField
            fullWidth
            name="lastname"
            label="Last Name"
            value={formik.values.lastname}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            error={formik.touched.lastname && Boolean(formik.errors.lastname)}
            helperText={formik.touched.lastname && formik.errors.lastname}
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
            helperText={formik.touched.password && formik.errors.password}
          />
          {error && (
            <div className="text-red-500 text-sm">
              {typeof error === "string" ? error : error?.message}
            </div>
          )}
          <span>
            Do you have an account?{" "}
            <span
              className="underline text-blue-300 cursor-pointer"
              onClick={() => navigate("/login")}
            >
              Login
            </span>
          </span>
          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ backgroundColor: "black", py: "11px", fontWeight: "bold" }}
            disabled={loading}
          >
            {loading ? <RotatingLines {...props} /> : "Sign Up"}
          </Button>
        </form>
      </div>
    </div>
  );
};

export default Register;
