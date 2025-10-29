import {
  Button,
  Checkbox,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
} from "@mui/material";
import React, { useEffect, useState } from "react";
import LoadingAnimation from "../../../ui/LoadingAnimation";
import { useParams } from "react-router";
import { Role, UpdateUser, User } from "../../../types/userTypes";
import { api } from "../../../config/api";
import { useFormik } from "formik";
import {
  updateUserProfileValidationSchema,
} from "../../../validator/userValidator";
import { toast, ToastContainer } from "react-toastify";
import { toastCustomize } from "../../homePage/BannerCenter";

const ProfileAdminUser = () => {
  const { userId } = useParams();
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isLoadingUpdate, setIsLoadingUpdate] = useState<boolean>(false);
  const [user, setUser] = useState<User | null>(null);
  const [roles, setRoles] = useState<Role[] | []>([]);
  const [selectedRoles, setSelectedRoles] = useState<Role[]>([]);

  useEffect(() => {
    const getUser = async () => {
      setIsLoading(true);
      try {
        const res = await api.get(`/api/v1/users/byUserId/${userId}`);
        setUser(res.data);
      } catch (error) {
        console.error(error);
      } finally {
        setIsLoading(false);
      }
    };
    getUser();
  }, [userId]);

  useEffect(() => {
    const getRoles = async () => {
      try {
        const res = await api.get(`/api/v1/roles`);
        setRoles(res.data);
      } catch (error) {
        console.error(error);
      }
    };
    getRoles();
  }, []);

  useEffect(() => {
    if (user?.roleNames?.length && roles.length) {
      const matchedRoles = roles.filter((role) =>
        user.roleNames.includes(role.name)
      );
      setSelectedRoles(matchedRoles);
    }
  }, [user, roles]);

  const formik = useFormik({
    enableReinitialize: true,
    initialValues: {
      firstname: user?.firstname ?? "",
      lastname: user?.lastname ?? "",
      newPassword: "",
      dateOfBirth: user?.dateOfBirth ?? "",
      accountLocked: user?.accountLocked ?? false,
      enabled: user?.enabled ?? true,
    },
    validationSchema: updateUserProfileValidationSchema,
    onSubmit: async (values: UpdateUser) => {
      const payload = {
        ...values,
        roleIds: selectedRoles.map((r) => r.id),
      };
      updateProfile(payload);
    },
  });

  const updateProfile = async (values: UpdateUser) => {
    try {
      setIsLoadingUpdate(true);
      await api.put(`/api/v1/users/updateUser/${userId}`, values);
      formik.setFieldValue("newPassword", "");
         toast.info("User updated successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
          toast.error(
              "Something went wrong while updating user.",
              toastCustomize
            );
    } finally {
      setIsLoadingUpdate(false);
    }
  };

  const handleToggleRole = (role: Role) => {
    setSelectedRoles((prevSelected) =>
      prevSelected.some((r) => r.id === role.id)
        ? prevSelected.filter((r) => r.id !== role.id)
        : [...prevSelected, role]
    );
  };

  return (
    <div>
      {user === null || isLoading ? (
        <div className="fixed top-0 left-0 flex h-full w-full z-50 bg-white opacity-80 items-center justify-center">
          <LoadingAnimation />
        </div>
      ) : (
        <div className="w-[500px] flex flex-col gap-4 pb-6 border-b-[1px] border-gray-200 max-md:w-full p-4">
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
            name="newPassword"
            label="Password"
            value={formik.values.newPassword}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            error={
              formik.touched.newPassword && Boolean(formik.errors.newPassword)
            }
            helperText={formik.touched.newPassword && formik.errors.newPassword}
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
          <FormControl className="flex-col">
            <InputLabel id="role-select-label">Enabled</InputLabel>
            <Checkbox
              name="enabled"
              checked={formik.values.enabled}
              onChange={formik.handleChange}
            />
          </FormControl>
          <FormControl>
            <InputLabel id="role-select-label">Account Locked</InputLabel>
            <Checkbox
              name="accountLocked"
              checked={formik.values.accountLocked}
              onChange={formik.handleChange}
            />
          </FormControl>
          <FormControl fullWidth>
            <InputLabel id="role-select-label">Roles</InputLabel>
            <Select
              labelId="role-select-label"
              multiple
              value={selectedRoles}
              label="Roles"
              renderValue={(selected) =>
                selected.map((role) => role.name).join(", ")
              }
            >
              {roles?.map((role) => (
                <MenuItem
                  key={role.id}
                  value={role.id}
                  onClick={(e) => {
                    e.preventDefault();
                    handleToggleRole(role);
                  }}
                  selected={selectedRoles.some((r) => r.id === role.id)}
                >
                  {role.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Button
            onClick={() => formik.handleSubmit()}
            className="w-full h-full"
            variant="contained"
            sx={{ py: "11px", backgroundColor: "black", fontWeight: "bold" }}
          >
            {isLoadingUpdate ? (
              <div className="flex items-center justify-center h-[100%]">
                <LoadingAnimation height={30} width={30} />
              </div>
            ) : (
              "Update"
            )}
          </Button>
        </div>
      )}
               <ToastContainer />
    </div>
  );
};

export default ProfileAdminUser;
