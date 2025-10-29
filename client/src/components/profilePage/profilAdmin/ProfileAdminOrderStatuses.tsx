import React, { useEffect, useState } from "react";
import { OrderStatus } from "../../../types/userTypes";
import { api } from "../../../config/api";
import { toastCustomize } from "./ProfileAdminAddProducts";
import { toast, ToastContainer } from "react-toastify";
import { useFormik } from "formik";
import { orderStatusSchema } from "../../../validator/userValidator";
import LoadingAnimation from "../../../ui/LoadingAnimation";
import { TextField } from "@mui/material";
import EditNoteIcon from "@mui/icons-material/EditNote";
import DeleteIcon from "@mui/icons-material/Delete";

const ProfileAdminOrderStatuses = () => {
  const [editActive, setEditActive] = useState<number | null>(null);
  const [refresh, setRefresh] = useState<boolean>(false);
  const [openDelete, setOpenDelete] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [orderStatuses, setOrderStatuses] = useState<OrderStatus[] | []>([]);

  useEffect(() => {
    const getOrderStatuses = async () => {
      try {
        setIsLoading(true);
        const res = await api.get("/api/v1/order-status/all");
        setOrderStatuses(res.data);
      } catch (error) {
        console.error(error);
      } finally {
        setIsLoading(false);
      }
    };
    getOrderStatuses();
  }, [refresh]);

  const addOrderStatus = async (values: { status: string }) => {
    try {
      await api.post("/api/v1/order-status/create", values);
      setRefresh((prev) => !prev);
      toast.success("Order status added successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while adding order status.",
        toastCustomize
      );
    }
  };

  useEffect(() => {
    if (editActive !== null) {
      const orderStatus = orderStatuses.find((os) => os.id === editActive);

      if (orderStatus) {
        editFormik.setFieldValue("id", orderStatus.id);
        editFormik.setFieldValue("status", orderStatus.status);
      }
    }
  }, [editActive, orderStatuses]);

  const formik = useFormik({
    initialValues: {
      status: "",
    },
    validationSchema: orderStatusSchema,
    onSubmit: (values: { status: string }) => {
      addOrderStatus(values);
      formik.resetForm();
    },
  });

  const editOrderStatus = async (
    id: number | null,
    values: {
      status: string;
    }
  ) => {
    try {
      await api.put(`/api/v1/order-status/${id}`, values);
      setRefresh((prev) => !prev);
      toast.info("Order status added successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while adding order status.",
        toastCustomize
      );
    }
  };

  const editFormik = useFormik({
    initialValues: {
      id: editActive,
      status: editActive
        ? orderStatuses.find((orderStatus) => orderStatus.id === editActive)
            ?.status ?? ""
        : "",
    },
    validationSchema: orderStatusSchema,
    onSubmit: (values) => {
      if (values.id !== null && values.id !== undefined) {
        editOrderStatus(values.id, {
          status: values.status,
        });
        setEditActive(null);
        editFormik.resetForm();
      } else {
        console.error("ID is not valid:", values.id);
      }
    },
  });

  const deleteOrderStatus = async (id: number) => {
    try {
      await api.delete(`/api/v1/order-status/${id}`);
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
            Order Statuses
          </span>
          <div className="flex flex-col gap-6">
            <div className="flex flex-wrap gap-4 items-center">
              {orderStatuses.map((orderStatus) => (
                <div
                  key={orderStatus.id}
                  className="flex gap-2 items-center bg-gray-200 rounded-xl p-2 max-md:rounded-md"
                >
                  {editActive === orderStatus?.id ? (
                    <form
                      className="flex items-center"
                      onSubmit={editFormik.handleSubmit}
                    >
                      <div className="flex items-start gap-2 px-4 text-sm flex-wrap">
                        <TextField
                          className="w-[160px] max-md:w-full"
                          name="status"
                          label="Order Status Name"
                          value={editFormik.values.status || orderStatus.status}
                          onChange={editFormik.handleChange}
                          onBlur={editFormik.handleBlur}
                          error={
                            editFormik.touched.status &&
                            Boolean(editFormik.errors.status)
                          }
                          helperText={
                            editFormik.touched.status &&
                            editFormik.errors.status
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
                        <span>Order Status:</span>
                        <span className="font-semibold">
                          {orderStatus.status}
                        </span>
                      </div>
                      <EditNoteIcon
                        className="cursor-pointer hover:opacity-50"
                        onClick={() => setEditActive(orderStatus?.id ?? null)}
                      />
                      <DeleteIcon
                        className="text-red-500 cursor-pointer hover:opacity-50"
                        onClick={() => setOpenDelete(orderStatus.id ?? null)}
                      />
                    </>
                  )}
                </div>
              ))}
            </div>
            <span className="font-bold">Add Order Status</span>
            <form className="flex items-center" onSubmit={formik.handleSubmit}>
              <div className="flex items-start gap-4 flex-wrap">
                <TextField
                  style={{ width: "300px" }}
                  name="status"
                  label="Order Status"
                  value={formik.values.status ?? ""}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  error={formik.touched.status && Boolean(formik.errors.status)}
                  helperText={formik.touched.status && formik.errors.status}
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
                  Are you sure to delete orders status:{" "}
                  {orderStatuses.map((orderStatus) => {
                    if (orderStatus.id === openDelete) {
                      return orderStatus.status;
                    }
                    return null;
                  })}
                </span>
                <div className="flex gap-4">
                  <div
                    onClick={() => deleteOrderStatus(openDelete)}
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

export default ProfileAdminOrderStatuses;
