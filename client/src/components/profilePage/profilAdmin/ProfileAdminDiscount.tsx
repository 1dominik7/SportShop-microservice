import { useEffect, useState } from "react";
import LoadingAnimation from "../../../ui/LoadingAnimation";
import { useFormik } from "formik";
import { DiscountCode } from "../../../types/userTypes";
import { discountValidation } from "../../../validator/userValidator";
import { TextField } from "@mui/material";
import { api } from "../../../config/api";
import { toast, ToastContainer } from "react-toastify";
import { toastCustomize } from "./ProfileAdminAddProducts";
import EditNoteIcon from "@mui/icons-material/EditNote";
import DeleteIcon from "@mui/icons-material/Delete";

const ProfileAdminDiscount = () => {
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [editActive, setEditActive] = useState<number | null>(null);
  const [openDelete, setOpenDelete] = useState<number | null>(null);
  const [discounts, setDiscounts] = useState<DiscountCode[]>([]);
  const [refresh, setRefresh] = useState<boolean>(false);

  useEffect(() => {
    const getDiscountCodes = async () => {
      try {
        setIsLoading(true);
        const res = await api.get("/api/v1/discount");
        setDiscounts(res.data);
      } catch (error) {
        console.error(error);
      } finally {
        setIsLoading(false);
      }
    };
    getDiscountCodes();
  }, [refresh]);

  useEffect(() => {
    if (editActive !== null) {
      const discount = discounts.find((discount) => discount.id === editActive);

      if (discount) {
        editFormik.setFieldValue("id", discount.id);
        editFormik.setFieldValue("name", discount.name);
        editFormik.setFieldValue("code", discount.code);
        editFormik.setFieldValue(
          "expiryDate",
          formatJavaLocalDateTimeForInput(discount?.expiryDate)
        );
        editFormik.setFieldValue("discount", discount.discount);
      }
    }
  }, [editActive, discounts]);

  const formatJavaLocalDateTimeForInput = (javaDate?: string): string => {
    if (!javaDate) return "";
    return javaDate.split("T")[0];
  };

  const addDiscount = async (values: {
    name: string;
    code: string;
    expiryDate: string;
    discount: number;
  }) => {
    try {
      const payload = {
        ...values,
        expiryDate: values.expiryDate ? `${values.expiryDate}T00:00:00` : null,
      };

      await api.post("/api/v1/discount", payload);
      setRefresh((prev) => !prev);
      toast.success("Discount coupon added successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while adding discount coupon.",
        toastCustomize
      );
    }
  };

  const formik = useFormik({
    initialValues: {
      name: "",
      code: "",
      expiryDate: "",
      discount: 0,
    },
    validationSchema: discountValidation,
    onSubmit: (values: {
      name: string;
      code: string;
      expiryDate: string;
      discount: number;
    }) => {
      addDiscount(values);
      formik.resetForm();
    },
  });

  const editDiscount = async (
    id: number | null,
    values: {
      name: string | undefined;
      code: string | undefined;
      expiryDate: string;
      discount: number | undefined;
    }
  ) => {
    try {
      const payload = {
        ...values,
        expiryDate: values.expiryDate ? `${values.expiryDate}T00:00:00` : null,
      };

      await api.put(`/api/v1/discount/${id}`, payload);
      setRefresh((prev) => !prev);
      toast.info("Discount updated successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while updating discount.",
        toastCustomize
      );
    }
  };

  const editFormik = useFormik({
    initialValues: {
      id: editActive,
      name: editActive
        ? discounts.find((item) => item.id === editActive)?.name
        : "",
      code: editActive
        ? discounts.find((item) => item.id === editActive)?.code
        : "",
      expiryDate: editActive
        ? formatJavaLocalDateTimeForInput(
            discounts.find((item) => item.id === editActive)?.expiryDate
          )
        : "",
      discount: editActive
        ? discounts.find((item) => item.id === editActive)?.discount
        : 0,
    },
    validationSchema: discountValidation,
    onSubmit: (values) => {
      if (values.id !== null && values.id !== undefined) {
        editDiscount(values.id, {
          name: values.name,
          code: values.code,
          expiryDate: values.expiryDate,
          discount: values.discount,
        });
        setEditActive(null);
        editFormik.resetForm();
      } else {
        console.error("ID is not valid:", values.id);
      }
    },
  });

  const deleteDiscountCode = async (id: number) => {
    try {
      await api.delete(`/api/v1/discount/${id}`);
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
          <span className="text-3xl font-bold max-md:text-xl">Discount</span>
          <div className="flex flex-col gap-6">
            <div className="flex flex-wrap gap-4 items-center">
              {discounts.map((discount) => (
                <div
                  key={discount.id}
                  className="flex gap-2 items-center bg-gray-200 rounded-xl p-2 max-md:rounded-md"
                >
                  {editActive === discount?.id ? (
                    <form
                      className="flex items-center"
                      onSubmit={editFormik.handleSubmit}
                    >
                      <div className="flex items-start gap-2 px-4 text-sm flex-wrap">
                        <TextField
                          className="w-[160px] max-md:w-full"
                          name="name"
                          label="Discount Name"
                          value={editFormik.values.name || discount.name}
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
                          name="code"
                          label="Discount Code"
                          value={editFormik.values.code || discount.code}
                          onChange={editFormik.handleChange}
                          onBlur={editFormik.handleBlur}
                          error={
                            editFormik.touched.code &&
                            Boolean(editFormik.errors.code)
                          }
                          helperText={
                            editFormik.touched.code && editFormik.errors.code
                          }
                        />
                        <TextField
                          className="w-[160px] max-md:w-full"
                          type="date"
                          name="expiryDate"
                          label="Expiry Date"
                          slotProps={{ inputLabel: { shrink: true } }}
                          value={
                            editFormik.values.expiryDate || discount.expiryDate
                          }
                          onChange={editFormik.handleChange}
                          onBlur={editFormik.handleBlur}
                          error={
                            editFormik.touched.expiryDate &&
                            Boolean(editFormik.errors.expiryDate)
                          }
                          helperText={
                            editFormik.touched.expiryDate &&
                            editFormik.errors.expiryDate
                          }
                        />
                        <TextField
                          className="w-[160px] max-md:w-full"
                          name="discount"
                          label="Discount Percentage %"
                          type="number"
                          value={
                            editFormik.values.discount || discount.discount
                          }
                          onChange={editFormik.handleChange}
                          onBlur={editFormik.handleBlur}
                          error={
                            editFormik.touched.discount &&
                            Boolean(editFormik.errors.discount)
                          }
                          helperText={
                            editFormik.touched.discount &&
                            editFormik.errors.discount
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
                      <div className="flex gap-[2px]">
                        <span>Discount Code:</span>
                        <span className="font-semibold">{discount.name}</span>
                      </div>
                      <EditNoteIcon
                        className="cursor-pointer hover:opacity-50"
                        onClick={() => setEditActive(discount?.id ?? null)}
                      />
                      <DeleteIcon
                        className="text-red-500 cursor-pointer hover:opacity-50"
                        onClick={() => setOpenDelete(discount.id ?? null)}
                      />
                    </>
                  )}
                </div>
              ))}
            </div>
            <span>Add Discount Code</span>
            <div className="flex flex-wrap gap-4 items-center">
              <form
                className="flex items-center"
                onSubmit={formik.handleSubmit}
              >
                <div className="flex items-start gap-4 flex-wrap">
                  <TextField
                    style={{ width: "300px" }}
                    name="name"
                    label="Discount Name"
                    value={formik.values.name}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    error={formik.touched.name && Boolean(formik.errors.name)}
                    helperText={formik.touched.name && formik.errors.name}
                  />
                  <TextField
                    style={{ width: "300px" }}
                    name="code"
                    label="Discount Code"
                    value={formik.values.code}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    error={formik.touched.code && Boolean(formik.errors.code)}
                    helperText={formik.touched.code && formik.errors.code}
                  />
                  <TextField
                    style={{ width: "300px" }}
                    type="date"
                    name="expiryDate"
                    label="Expiry Date"
                    slotProps={{ inputLabel: { shrink: true } }}
                    value={formik.values.expiryDate}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    error={
                      formik.touched.expiryDate &&
                      Boolean(formik.errors.expiryDate)
                    }
                    helperText={
                      formik.touched.expiryDate && formik.errors.expiryDate
                    }
                  />
                  <TextField
                    style={{ width: "300px" }}
                    name="discount"
                    label="Discount Percentage %"
                    type="number"
                    value={formik.values.discount}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    error={
                      formik.touched.discount && Boolean(formik.errors.discount)
                    }
                    helperText={
                      formik.touched.discount && formik.errors.discount
                    }
                  />

                  <button
                    className="h-[55px] px-6 bg-gray-200 rounded-xl border-[1px] border-black hover:opacity-80 font-bold"
                    type="submit"
                  >
                    Create discount
                  </button>
                </div>
              </form>
            </div>
          </div>
          {openDelete !== null && (
            <div className="absolute w-full h-full flex items-center justify-center top-0 left-0 ">
              <div className="absolute opacity-90 w-full h-full bg-white"></div>
              <div className="flex z-20 flex-col gap-4 items-center justify-center text-lg p-10 border-2 border-gray-200 rounded-xl bg-gray-100">
                <span className="font-bold">
                  Are you sure to delete discount coupon:{" "}
                  {discounts.map((discount) => {
                    if (discount.id === openDelete) {
                      return discount.name;
                    }
                    return null;
                  })}
                </span>
                <div className="flex gap-4">
                  <div
                    onClick={() => deleteDiscountCode(openDelete)}
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

export default ProfileAdminDiscount;
