import { Button, TextField } from "@mui/material";
import { useNavigate, useParams } from "react-router";
import { AppDispatch, useAppSelector } from "../../state/store";
import { useDispatch } from "react-redux";
import { useFormik } from "formik";
import { resetPasswordValidationSchema } from "../../validator/userValidator";
import { RestartPasswordType } from "../../types/userTypes";
import { RotatingLines } from "react-loader-spinner";
import { resetPassword } from "../../state/authActions";

const props = {
  visible: true,
  height: "30",
  width: "30",
  color: "#6BE140",
  strokeWidth: "5",
  animationDuration: "0.75",
  ariaLabel: "rotating-lines-loading",
};

const ResetPassword = () => {
  const { token } = useParams();
  const navigate = useNavigate();
  const dispatch: AppDispatch = useDispatch();
  const loading = useAppSelector((store) => store.auth.loading);
  const error = useAppSelector((store) => store.auth.error);
  const formik = useFormik({
    initialValues: {
      newPassword: "",
      confirmPassword: "",
    },
    validationSchema: resetPasswordValidationSchema,
    onSubmit: (values: RestartPasswordType) => {
      if (!token) return;
      dispatch(
        resetPassword(
          {
            token,
            newPassword: values.newPassword,
            confirmPassword: values.confirmPassword,
          },
          navigate
        )
      );
    },
  });

  return (
    <div className="w-full h-screen flex flex-col items-center justify-center space-y-6">
      <h1 className="text-center font-bold text-3xl">Restart you password</h1>
      <span>Enter your email</span>
      <div className="w-[500px] space-y-5">
        <TextField
          fullWidth
          name="newPassword"
          label="New Password"
          type="password"
          value={formik.values.newPassword}
          onChange={formik.handleChange}
          onBlur={formik.handleBlur}
          error={
            formik.touched.newPassword && Boolean(formik.errors.newPassword)
          }
          helperText={formik.touched.newPassword && formik.errors?.newPassword}
        />
        <TextField
          fullWidth
          name="confirmPassword"
          label="Confirm Password"
          type="password"
          value={formik.values.confirmPassword}
          onChange={formik.handleChange}
          onBlur={formik.handleBlur}
          error={
            formik.touched.confirmPassword &&
            Boolean(formik.errors.confirmPassword)
          }
          helperText={
            formik.touched.confirmPassword && formik.errors?.confirmPassword
          }
        />
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
  );
};

export default ResetPassword;
