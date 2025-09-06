import { Button, TextField } from "@mui/material";
import { useFormik } from "formik";
import { ProfileChangeType } from "../../types/userTypes";
import { api } from "../../config/api";
import { useAppDispatch, useAppSelector } from "../../state/store";
import LoadingAnimation from "../../ui/LoadingAnimation";
import { updateProfileValidationSchema } from "../../validator/userValidator";
import { fetchUserProfileSuccess } from "../../state/authSlice";

const ProfilePersonalData = () => {
  const dispatch = useAppDispatch();
  const { user, isLoggedIn } = useAppSelector((store) => store.auth);
  const formik = useFormik({
    enableReinitialize: true,
    initialValues: {
      firstname: user?.firstname ?? "",
      lastname: user?.lastname ?? "",
      dateOfBirth: user?.dateOfBirth ?? "",
    },
    validationSchema: updateProfileValidationSchema,
    onSubmit: async (values: ProfileChangeType) => {
      updateProfile(values);
    },
  });

  const updateProfile = async (values: ProfileChangeType) => {
    try {
      const res = await api.put(`/api/v1/users/profile`, values);
      dispatch(fetchUserProfileSuccess(res.data));
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div className="relative py-11 px-20 max-md:px-6">
      {!user || !isLoggedIn ? (
        <div className="fixed top-0 left-0 flex h-full w-full z-50 bg-white opacity-80 items-center justify-center">
          <LoadingAnimation />
        </div>
      ) : (
        <div className="w-[500px] flex flex-col gap-4 pb-6 border-b-[1px] border-gray-200 max-md:w-full">
          <TextField
            fullWidth
            name="firstname"
            label="Firstname"
            value={formik.values.firstname}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            error={formik.touched.firstname && Boolean(formik.errors.firstname)}
            helperText={formik.touched.firstname && formik.errors.firstname}
          />
          <TextField
            fullWidth
            name="lastname"
            label="Lastname"
            value={formik.values.lastname}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            error={formik.touched.lastname && Boolean(formik.errors.lastname)}
            helperText={formik.touched.lastname && formik.errors.lastname}
          />
          <TextField
            fullWidth
            type="date"
            name="dateOfBirth"
            label="Date Of Birth"
            InputLabelProps={{
              shrink: true,
            }}
            value={formik.values.dateOfBirth}
            onChange={formik.handleChange}
          />
          <TextField
            fullWidth
            className="bg-slate-300"
            label="Email"
            InputProps={{
              readOnly: true,
            }}
            value={user?.email ?? ""}
          />
          <TextField
            fullWidth
            type="date"
            className="bg-slate-300"
            label="Created date"
            InputProps={{
              readOnly: true,
            }}
            value={user?.createdDate ? user.createdDate.slice(0, 10) : ""}
          />
          <Button
            onClick={() => formik.handleSubmit()}
            className="w-full"
            variant="contained"
            sx={{ py: "11px", backgroundColor: "black", fontWeight: "bold" }}
          >
            Save
          </Button>
        </div>
      )}
    </div>
  );
};

export default ProfilePersonalData;
