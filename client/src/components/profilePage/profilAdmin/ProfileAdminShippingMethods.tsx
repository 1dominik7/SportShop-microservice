import React, { useEffect, useState } from "react";
import { api } from "../../../config/api";
import { ShippingMethod } from "../../../types/userTypes";
import { useFormik } from "formik";
import { shippingMethodSchema } from "../../../validator/userValidator";
import { toast, ToastContainer } from "react-toastify";
import { toastCustomize } from "./ProfileAdminAddProducts";
import { TextField } from "@mui/material";
import EditNoteIcon from "@mui/icons-material/EditNote";
import DeleteIcon from "@mui/icons-material/Delete";
import LoadingAnimation from "../../../ui/LoadingAnimation";

const ProfileAdminShippingMethods = () => {
  const [editActive, setEditActive] = useState<number | null>(null);
  const [refresh, setRefresh] = useState<boolean>(false);
  const [openDelete, setOpenDelete] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [shippingMethods, setShippingMethods] = useState<ShippingMethod[] | []>(
    []
  );

  useEffect(() => {
    const getShippingMethods = async () => {
      try {
        setIsLoading(true);
        const res = await api.get("/api/v1/shipping-method/all");
        setShippingMethods(res.data);
      } catch (error) {
        console.error(error);
      } finally {
        setIsLoading(false);
      }
    };
    getShippingMethods();
  }, [refresh]);

  const addShippingMethod = async (values: { name: string; price: number }) => {
    try {
      await api.post("/api/v1/shipping-method", values);
      setRefresh((prev) => !prev);
      toast.success("Shipping method added successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while adding shipping method.",
        toastCustomize
      );
    }
  };

  useEffect(() => {
    if (editActive !== null) {
      const shippingMethod = shippingMethods.find((sp) => sp.id === editActive);

      if (shippingMethod) {
        editFormik.setFieldValue("id", shippingMethod.id);
        editFormik.setFieldValue("name", shippingMethod.name);
        editFormik.setFieldValue("price", shippingMethod.price);
      }
    }
  }, [editActive, shippingMethods]);

  const formik = useFormik({
    initialValues: {
      name: "",
      price: 0,
    },
    validationSchema: shippingMethodSchema,
    onSubmit: (values: { name: string; price: number }) => {
      addShippingMethod(values);
      formik.resetForm();
    },
  });

  const editShippingMethod = async (
    id: number | null,
    values: {
      name: string;
      price: number;
    }
  ) => {
    try {
      await api.put(`/api/v1/shipping-method/${id}`, values);
      setRefresh((prev) => !prev);
      toast.info("Shipping method added successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while adding shipping method.",
        toastCustomize
      );
    }
  };

  const editFormik = useFormik({
    initialValues: {
      id: editActive,
      name: editActive
        ? shippingMethods.find(
            (shippingMethod) => shippingMethod.id === editActive
          )?.name ?? ""
        : "",
      price: editActive
        ? shippingMethods.find(
            (shippingMethod) => shippingMethod.id === editActive
          )?.price ?? 0
        : 0,
    },
    validationSchema: shippingMethodSchema,
    onSubmit: (values) => {
      if (values.id !== null && values.id !== undefined) {
        editShippingMethod(values.id, {
          name: values.name,
          price: values.price,
        });
        setEditActive(null);
        editFormik.resetForm();
      } else {
        console.error("ID is not valid:", values.id);
      }
    },
  });

  const deleteVariationOption = async (id: number) => {
    try {
      await api.delete(`/api/v1/shipping-method/${id}`);
      setRefresh((prev) => !prev);
      setOpenDelete(null);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div className="w-full h-full relative flex flex-col gap-4 p-6">
      {isLoading ? (
        <div className="flex items-center justify-center w-full h-full z-50 bg-white opacity-80">
          <LoadingAnimation />
        </div>
      ) : (
        <>
          <span className="text-3xl font-bold max-md:text-xl">
            Shipping Methods
          </span>
          <div className="flex flex-col gap-6">
            <div className="flex flex-wrap gap-4 items-center">
              {shippingMethods.map((shippingMethod) => (
                <div
                  key={shippingMethod.id}
                  className="flex gap-2 items-center bg-gray-200 rounded-xl p-2 max-md:rounded-md"
                >
                  {editActive === shippingMethod?.id ? (
                    <form
                      className="flex items-center"
                      onSubmit={editFormik.handleSubmit}
                    >
                      <div className="flex items-start gap-2 px-4 text-sm flex-wrap">
                        <TextField
                          className="w-[160px] max-md:w-full"
                          name="name"
                          label="Shipping Method Name"
                          value={editFormik.values.name || shippingMethod.name}
                          onChange={editFormik.handleChange}
                          onBlur={editFormik.handleBlur}
                          error={
                            editFormik.touched.name &&
                            Boolean(editFormik.errors.name)
                          }
                          helperText={
                            editFormik.touched.name && editFormik.errors.name
                          }
                        />
                        <TextField
                          className="w-[160px] max-md:w-full"
                          name="price"
                          label="Shipping Method Price"
                          value={
                            editFormik.values.price || shippingMethod.price
                          }
                          onChange={editFormik.handleChange}
                          onBlur={editFormik.handleBlur}
                          error={
                            editFormik.touched.price &&
                            Boolean(editFormik.errors.price)
                          }
                          helperText={
                            editFormik.touched.price && editFormik.errors.price
                          }
                        />
                        <button
                          className="h-[55px] ml-2 px-6 bg-gray-200 rounded-xl border-[1px] border-black hover:opacity-80 font-bold"
                          type="submit"
                        >
                          Submit
                        </button>
                        <button
                          className="h-[55px] ml-2 px-6 bg-gray-200 rounded-xl border-[1px] border-black hover:opacity-80 font-bold"
                          onClick={() => setEditActive(null)}
                        >
                          Cancel
                        </button>
                      </div>
                    </form>
                  ) : (
                    <>
                      <div className="flex gap-[2px] px-2">
                        <span>ShippingMethod:</span>
                        <span className="font-semibold">
                          {shippingMethod.name}
                        </span>
                      </div>
                      <EditNoteIcon
                        className="cursor-pointer hover:opacity-50"
                        onClick={() =>
                          setEditActive(shippingMethod?.id ?? null)
                        }
                      />
                      <DeleteIcon
                        className="text-red-500 cursor-pointer hover:opacity-50"
                        onClick={() => setOpenDelete(shippingMethod.id ?? null)}
                      />
                    </>
                  )}
                </div>
              ))}
            </div>
            <span className="font-bold">Add Shipping Method</span>
            <form className="flex items-center" onSubmit={formik.handleSubmit}>
              <div className="flex items-start gap-4 flex-wrap">
                <TextField
                  style={{ width: "300px" }}
                  name="name"
                  label="Shipping Method Name"
                  value={formik.values.name ?? ""}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  error={formik.touched.name && Boolean(formik.errors.name)}
                  helperText={formik.touched.name && formik.errors.name}
                />
                <TextField
                  style={{ width: "300px" }}
                  name="price"
                  label="Shipping Method Price"
                  value={formik.values.price ?? ""}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  error={formik.touched.price && Boolean(formik.errors.price)}
                  helperText={formik.touched.price && formik.errors.price}
                />
                <button
                  className="h-[55px] ml-4 px-6 bg-gray-200 rounded-xl border-[1px] border-black hover:opacity-80 font-bold max-md:ml-0"
                  type="submit"
                >
                  Submit
                </button>
              </div>
            </form>
          </div>
          {openDelete !== null && (
            <div className="absolute w-full h-full flex items-center justify-center top-0 left-0">
              <div className="absolute opacity-90 w-full h-full bg-white"></div>
              <div className="flex z-20 flex-col gap-4 items-center justify-center text-lg p-10 border-2 border-gray-200 rounded-xl bg-gray-100 max-md:text-base">
                <span className="font-bold">
                  Are you sure to delete shipping method:{" "}
                  {shippingMethods.map((shippingMethod) => {
                    if (shippingMethod.id === openDelete) {
                      return shippingMethod.name;
                    }
                    return null;
                  })}
                </span>
                <div className="flex gap-4">
                  <div
                    onClick={() => deleteVariationOption(openDelete)}
                    className="w-[100px] text-center py-2 rounded-full bg-gray-300 cursor-pointer text-red-500 font-bold hover:opacity-80"
                  >
                    Yes
                  </div>
                  <div
                    onClick={() => setOpenDelete(null)}
                    className="w-[100px] py-2 rounded-full bg-gray-300 cursor-pointer font-bold text-center hover:opacity-80"
                  >
                    Cancel
                  </div>
                </div>
              </div>
            </div>
          )}
          <ToastContainer />
        </>
      )}
    </div>
  );
};

export default ProfileAdminShippingMethods;
