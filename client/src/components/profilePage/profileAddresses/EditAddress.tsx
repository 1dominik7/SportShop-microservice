import React, { useEffect, useState } from "react";
import { Address } from "../../../types/userTypes";
import { addressSchema } from "../../../validator/userValidator";
import { useFormik } from "formik";
import { api } from "../../../config/api";
import { toastCustomize } from "../profilAdmin/ProfileAdminAddProducts";
import { toast } from "react-toastify";
import {
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
} from "@mui/material";

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

interface Prop {
  setOpenEditAddress: React.Dispatch<React.SetStateAction<number | null>>;
  openEditAddress: number | null;
  refetch: () => void;
}

const EditAddress = ({
  setOpenEditAddress,
  openEditAddress,
  refetch,
}: Prop) => {
  const [data, setData] = useState<Address | null>(null);

  useEffect(() => {
    const getAddressById = async () => {
      try {
        const res = await api.get(
          `/api/v1/address/byId/${openEditAddress}`
        );
        setData(res.data);
      } catch (error) {
        console.error(error);
      }
    };
    getAddressById();
  }, [openEditAddress]);

  const formik = useFormik({
    enableReinitialize: true,
    initialValues: {
      country: data?.country ?? "",
      city: data?.city ?? "",
      firstName: data?.firstName ?? "",
      lastName: data?.lastName ?? "",
      postalCode: data?.postalCode ?? "",
      street: data?.street ?? "",
      phoneNumber: data?.phoneNumber ?? "",
      addressLine1: data?.addressLine1 ?? "",
      addressLine2: data?.addressLine2 ?? "",
    },
    validationSchema: addressSchema,
    onSubmit: async (values: Address) => {
      await saveEditedAddress(values);
      formik.resetForm();
      setOpenEditAddress(null);
      refetch();
    },
  });

  const saveEditedAddress = async (values: Address) => {
    try {
      await api.put(
        `/api/v1/address/${openEditAddress}`,
        values
      );
      toast.success("Address updated successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while updating the address.",
        toastCustomize
      );
    }
  };

  return (
    <div>
      <div className="fixed flex items-center justify-center top-0 left-0 h-full w-full z-50">
        <div
          className="fixed w-full h-full opacity-80 z-10 bg-white"
          onClick={() => setOpenEditAddress(null)}
        />
        <div className="fixed bg-white z-20 flex items-center justify-center py-12 px-16 rounded-md border-[1px] border-gray-200 max-sm:py-4 max-md:px-4 ">
          <div className="w-[500px] flex flex-col gap-6 max-sm:gap-2 max-md:w-[300px] overflow-y-scroll">
            <FormControl
              margin="normal"
              fullWidth
            >
              <InputLabel id="selector-label">Select Country</InputLabel>
              <Select
                labelId="selector-label"
                name="country"
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
              value={formik.values.lastName ?? ""}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
              error={formik.touched.lastName && Boolean(formik.errors.lastName)}
              helperText={formik.touched.lastName && formik.errors.lastName}
            />
            <TextField
              fullWidth
              name="postalCode"
              label="Postal Code"
              value={formik.values.postalCode ?? ""}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
              error={
                formik.touched.postalCode && Boolean(formik.errors.postalCode)
              }
              helperText={formik.touched.postalCode && formik.errors.postalCode}
            />
            <TextField
              fullWidth
              name="street"
              label="Street"
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
              value={formik.values.phoneNumber ?? ""}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
              error={
                formik.touched.phoneNumber && Boolean(formik.errors.phoneNumber)
              }
              helperText={
                formik.touched.phoneNumber && formik.errors.phoneNumber
              }
            />
            <TextField
              fullWidth
              name="addressLine1"
              label="Address Line 1"
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
            <div className="flex items-center justify-end gap-6">
              <button
                className="w-[150px] p-3 border-[1px] border-black text-xl font-semibold rounded-full hover:bg-gray-500 hover:border-gray-500 hover:text-white max-md:w-full max-md:p-2 max-sm:text-sm"
                onClick={() => setOpenEditAddress(null)}
              >
                Cancel
              </button>
              <button
                className="w-[150px] p-3 bg-black border-[1px] border-black text-white text-xl font-semibold rounded-full max-md:w-full max-md:p-2 max-sm:text-sm"
                type="button"
                onClick={() => formik.handleSubmit()}
              >
                Save
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EditAddress;
