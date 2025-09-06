import { useState } from "react";
import { useFormik } from "formik";
import { addressSchema } from "../../../validator/userValidator";
import {
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  useMediaQuery,
  useTheme,
} from "@mui/material";
import { api } from "../../../config/api";
import { useAppSelector } from "../../../state/store";
import { Address } from "../../../types/userTypes";
import { toast } from "react-toastify";
import { toastCustomize } from "../profilAdmin/ProfileAdminAddProducts";
import { useUserAddresses } from "../../../hooks/query";
import DeleteForeverIcon from "@mui/icons-material/DeleteForever";
import EditIcon from "@mui/icons-material/Edit";
import EditAddress from "./EditAddress";
import LoadingAnimation from "../../../ui/LoadingAnimation";

const countries = [
  { id: 1, country: "Poland" },
  { id: 2, country: "Czech Republic" },
  { id: 3, country: "England" },
  { id: 4, country: "Germany" },
  { id: 5, country: "Slovakia" },
  { id: 6, country: "France" },
  { id: 7, country: "Italy" },
  { id: 8, country: "Spain" },
];

const ProfileAddresses = () => {
  const auth = useAppSelector((store) => store?.auth);
  const userId = auth.user?.id;
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

  const { data, refetch, isFetching } = useUserAddresses();

  const [openNewAddress, setOpenNewAddress] = useState(false);
  const [openEditAddress, setOpenEditAddress] = useState<number | null>(null);

  const formik = useFormik({
    initialValues: {
      country: "Poland",
      city: "",
      firstName: "",
      lastName: "",
      postalCode: "",
      street: "",
      phoneNumber: "",
      addressLine1: "",
      addressLine2: "",
    },
    validationSchema: addressSchema,
    onSubmit: async (values: Address) => {
      await addAddress(values);
      formik.resetForm();
      setOpenNewAddress(false);
      refetch();
    },
  });

  const addAddress = async (values: Address) => {
    try {
      await api.post(`/api/v1/address/create`, values);
      toast.success("Address added successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while adding the address.",
        toastCustomize
      );
    }
  };

  const deleteAddress = async (addressId: number) => {
    try {
      await api.delete(`/api/v1/address/${addressId}?userId=${userId}`);
      toast.success("Address deleted successfully!", toastCustomize);
      refetch();
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while deleting the address.",
        toastCustomize
      );
    }
  };

  return (
    <div className="relative py-11 px-20 max-lg:px-10 max-md:py-5 max-md:px-6">
      <div className="flex gap-4 pb-6 items-center border-b-[1px] border-gray-200 max-md:flex-col">
        <h1 className="text-3xl font-bold max-sm:text-2xl max-md:text-center">
          CORRESPONDENCE ADDRESS
        </h1>
        <button
          className="p-4 text-sm bg-black rounded-full text-white font-semibold"
          onClick={() => setOpenNewAddress(true)}
        >
          New correspondence address
        </button>
      </div>
      {data && data?.length > 0 && (
        <div className="flex flex-grow py-4 gap-4 max-sm:flex-col">
          {isFetching && (
            <div className="absolute top-0 left-0 flex w-full h-full justify-center items-center bg-white opacity-60 z-50">
              <LoadingAnimation />
            </div>
          )}
          {data.map((address) => (
            <div
              key={address.id}
              className="w-[300px] h-[300px] flex flex-col justify-between border-[1px] border-gray-300 p-6 max-sm:w-full max-sm:h-full"
            >
              <div className="flex flex-col gap-[2px]">
                <div className="flex gap-2">
                  <span>{address?.firstName}</span>
                  <span>{address?.lastName}</span>
                </div>
                <div className="flex gap-2">
                  <span>{address?.street}</span>
                  <span>{address?.addressLine1}</span>
                  <span>{address?.addressLine2}</span>
                </div>
                <div className="flex gap-2">
                  <span>{address?.postalCode}</span>
                  <span>{address?.city}</span>
                </div>
                <span>{address?.phoneNumber}</span>
                <span>{address?.country}</span>
              </div>
              <div className="flex justify-end gap-4 items-center">
                <div
                  className="p-3 bg-gray-200 rounded-full cursor-pointer hover:bg-gray-100"
                  onClick={() => setOpenEditAddress(address.id!)}
                >
                  <EditIcon />
                </div>
                <div
                  className="p-3 bg-gray-200 rounded-full cursor-pointer hover:bg-gray-100"
                  onClick={() => deleteAddress(address.id!)}
                >
                  <DeleteForeverIcon />
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
      {openNewAddress && (
        <div className="fixed flex items-center justify-center top-0 left-0 h-full w-full z-50">
          <div
            className="fixed w-full h-full opacity-80 z-10 bg-white"
            onClick={() => setOpenNewAddress(false)}
          />
          <div className="fixed bg-white z-20 flex items-center justify-center py-12 px-16 rounded-md border-[1px] border-gray-200 max-sm:py-4 max-md:px-4 ">
            <div className="w-[500px] flex flex-col gap-6 max-sm:gap-2 max-md:w-[300px] overflow-y-scroll">
              <FormControl margin="normal" fullWidth>
                <InputLabel id="selector-label">Select Country</InputLabel>
                <Select
                  labelId="selector-label"
                  name="country"
                  size={isMobile ? "small" : "medium"}
                  value={formik.values.country}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  label="Select Country"
                >
                  <MenuItem value="">
                    <em>None</em>
                  </MenuItem>
                  {countries.map((country) => (
                    <MenuItem key={country?.id} value={country?.country}>
                      {country.country}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <TextField
                fullWidth
                name="city"
                label="City"
                size={isMobile ? "small" : "medium"}
                value={formik.values.city ?? ""}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                error={formik.touched.city && Boolean(formik.errors.city)}
                helperText={formik.touched.city && formik.errors.city}
              />
              <TextField
                fullWidth
                name="firstName"
                label="First Name"
                size={isMobile ? "small" : "medium"}
                value={formik.values.firstName ?? ""}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                error={
                  formik.touched.firstName && Boolean(formik.errors.firstName)
                }
                helperText={formik.touched.firstName && formik.errors.firstName}
              />
              <TextField
                fullWidth
                name="lastName"
                label="Last Name"
                size={isMobile ? "small" : "medium"}
                value={formik.values.lastName ?? ""}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                error={
                  formik.touched.lastName && Boolean(formik.errors.lastName)
                }
                helperText={formik.touched.lastName && formik.errors.lastName}
              />
              <TextField
                fullWidth
                name="postalCode"
                label="Postal Code"
                size={isMobile ? "small" : "medium"}
                value={formik.values.postalCode ?? ""}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                error={
                  formik.touched.postalCode && Boolean(formik.errors.postalCode)
                }
                helperText={
                  formik.touched.postalCode && formik.errors.postalCode
                }
              />
              <TextField
                fullWidth
                name="street"
                label="Street"
                size={isMobile ? "small" : "medium"}
                value={formik.values.street ?? ""}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                error={formik.touched.street && Boolean(formik.errors.street)}
                helperText={formik.touched.street && formik.errors.street}
              />
              <TextField
                fullWidth
                name="phoneNumber"
                label="Phone Number"
                size={isMobile ? "small" : "medium"}
                value={formik.values.phoneNumber ?? ""}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                error={
                  formik.touched.phoneNumber &&
                  Boolean(formik.errors.phoneNumber)
                }
                helperText={
                  formik.touched.phoneNumber && formik.errors.phoneNumber
                }
              />
              <TextField
                fullWidth
                name="addressLine1"
                label="Address Line 1"
                size={isMobile ? "small" : "medium"}
                value={formik.values.addressLine1 ?? ""}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                error={
                  formik.touched.addressLine1 &&
                  Boolean(formik.errors.addressLine1)
                }
                helperText={
                  formik.touched.addressLine1 && formik.errors.addressLine1
                }
              />
              <TextField
                fullWidth
                name="addressLine2"
                label="Address Line 2"
                size={isMobile ? "small" : "medium"}
                value={formik.values.addressLine2 ?? ""}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                error={
                  formik.touched.addressLine2 &&
                  Boolean(formik.errors.addressLine2)
                }
                helperText={
                  formik.touched.addressLine2 && formik.errors.addressLine2
                }
              />
              <div className="flex items-center justify-end gap-6 mt-4 max-sm:mt-2">
                <button
                  className="w-[150px] p-3 border-[1px] border-black text-xl font-semibold rounded-full hover:bg-gray-500 hover:border-gray-500 hover:text-white max-md:w-full max-md:p-2 max-sm:text-sm"
                  onClick={() => setOpenNewAddress(false)}
                >
                  Cancel
                </button>
                <button
                  className="w-[150px] p-3 bg-black border-[1px] border-black text-white text-xl font-semibold rounded-full max-md:w-full max-md:p-2 max-sm:text-sm"
                  onClick={() => formik.handleSubmit()}
                >
                  Save
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
      {openEditAddress !== null && (
        <div>
          <EditAddress
            setOpenEditAddress={setOpenEditAddress}
            openEditAddress={openEditAddress}
            refetch={refetch}
          />
        </div>
      )}
    </div>
  );
};

export default ProfileAddresses;
