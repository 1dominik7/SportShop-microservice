import { useEffect, useState } from "react";
import { api } from "../../../config/api";
import { Category } from "../../../types/userTypes";
import EditNoteIcon from "@mui/icons-material/EditNote";
import DeleteIcon from "@mui/icons-material/Delete";
import { useFormik } from "formik";
import {
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
} from "@mui/material";
import { categorySchema } from "../../../validator/userValidator";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { toastCustomize } from "./ProfileAdminAddProducts";
import LoadingAnimation from "../../../ui/LoadingAnimation";

const ProfileAdminCategories = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [refresh, setRefresh] = useState<boolean>(false);
  const [editActive, setEditActive] = useState<number | null>(null);
  const [openDelete, setOpenDelete] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  useEffect(() => {
    const getCategories = async () => {
      try {
        setIsLoading(true);
        const res = await api.get("/api/v1/category/all");
        setCategories(res.data);
      } catch (error) {
        console.error(error);
      } finally {
        setIsLoading(false);
      }
    };
    getCategories();
  }, [refresh]);

  useEffect(() => {
    if (editActive !== null) {
      const category = categories.find(
        (category) => category.id === editActive
      );

      if (category) {
        editFormik.setFieldValue("id", category.id);
        editFormik.setFieldValue("categoryName", category.categoryName);
        editFormik.setFieldValue("parentCategoryId", category.parentCategoryId);
      } else {
        editFormik.setFieldValue("parentCategoryId", null);
      }
    }
  }, [editActive, categories]);

  const addCategory = async (values: {
    categoryName: string;
    parentCategoryId: number | null;
  }) => {
    try {
      await api.post("/api/v1/category/create", values);
      setRefresh((prev) => !prev);
      toast.success("Category added successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while adding category.",
        toastCustomize
      );
    }
  };

  const formik = useFormik({
    initialValues: {
      categoryName: "",
      parentCategoryId: null,
    },
    validationSchema: categorySchema,
    onSubmit: (values: {
      categoryName: string;
      parentCategoryId: number | null;
    }) => {
      addCategory(values);
      formik.resetForm();
    },
  });

  const editCategory = async (
    id: number | null,
    values: {
      categoryName: string | undefined;
      parentCategoryId: number | null;
    }
  ) => {
    try {
      await api.put(`/api/v1/category/${id}`, values);
      setRefresh((prev) => !prev);
      toast.info("Category added successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while adding category.",
        toastCustomize
      );
    }
  };

  const editFormik = useFormik({
    initialValues: {
      id: editActive,
      categoryName: editActive
        ? categories.find((category) => category.id === editActive)
            ?.categoryName
        : "",
      parentCategoryId: editActive
        ? categories?.find(
            (category) => category?.parentCategoryId === editActive
          )?.id ?? null
        : null,
    },
    validationSchema: categorySchema,
    onSubmit: (values) => {
      if (values.id !== null && values.id !== undefined) {
        editCategory(values.id, {
          categoryName: values.categoryName,
          parentCategoryId: values.parentCategoryId,
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
      await api.delete(`/api/v1/category/${id}`);
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
          <span className="text-3xl font-bold max-md:text-xl">Categories</span>
          <div className="flex flex-col gap-6">
            <div className="flex flex-wrap gap-4 items-center">
              {categories.map((category) => (
                <div
                  key={category.id}
                  className="flex gap-2 items-center bg-gray-200 rounded-xl p-2 max-md:rounded-md"
                >
                  {editActive === category?.id ? (
                    <form
                      className="flex items-center"
                      onSubmit={editFormik.handleSubmit}
                    >
                      <div className="flex items-start gap-2 px-4 text-sm flex-wrap">
                        <TextField
                          className="w-[160px] max-md:w-full"
                          name="categoryName"
                          label="Category Name"
                          value={
                            editFormik.values.categoryName ||
                            category.categoryName
                          }
                          onChange={editFormik.handleChange}
                          onBlur={editFormik.handleBlur}
                          error={
                            editFormik.touched.categoryName &&
                            Boolean(editFormik.errors.categoryName)
                          }
                          helperText={
                            editFormik.touched.categoryName &&
                            editFormik.errors.categoryName
                          }
                        />
                        <FormControl
                          className="w-[160px] max-md:w-full mt-0"
                        >
                          <InputLabel id="selector-label">
                            Select Category Parent
                          </InputLabel>
                          <Select
                            labelId="selector-label"
                            name="parentCategoryId"
                            value={editFormik.values.parentCategoryId ?? ""}
                            onChange={editFormik.handleChange}
                            onBlur={editFormik.handleBlur}
                            label="Select Category"
                          >
                            <MenuItem value="">
                              <em>None</em>
                            </MenuItem>
                            {categories
                              ?.filter(
                                (category) => category?.id !== editActive
                              )
                              .map((category) => (
                                <MenuItem
                                  key={category?.id}
                                  value={category?.id}
                                >
                                  {category?.categoryName}
                                </MenuItem>
                              ))}
                          </Select>
                        </FormControl>
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
                        <span>Category:</span>
                        <span className="font-semibold">
                          {category.categoryName}
                        </span>
                      </div>
                      <EditNoteIcon
                        className="cursor-pointer hover:opacity-50"
                        onClick={() => setEditActive(category?.id ?? null)}
                      />
                      <DeleteIcon
                        className="text-red-500 cursor-pointer hover:opacity-50"
                        onClick={() => setOpenDelete(category.id ?? null)}
                      />
                    </>
                  )}
                </div>
              ))}
            </div>
            <span className="font-bold">Add Category</span>
            <form className="flex items-center" onSubmit={formik.handleSubmit}>
              <div className="flex items-start gap-4 flex-wrap">
                <TextField
                  style={{ width: "300px" }}
                  name="categoryName"
                  label="Category Name"
                  value={formik.values.categoryName ?? ""}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  error={
                    formik.touched.categoryName &&
                    Boolean(formik.errors.categoryName)
                  }
                  helperText={
                    formik.touched.categoryName && formik.errors.categoryName
                  }
                />
                <FormControl
                  margin="normal"
                  style={{ width: "300px", marginTop: 0 }}
                >
                  <InputLabel id="selector-label">
                    Select Category Parent
                  </InputLabel>
                  <Select
                    labelId="selector-label"
                    name="parentCategoryId"
                    value={formik.values.parentCategoryId ?? ""}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    label="Select Category Parent"
                    MenuProps={{
                      PaperProps: {
                        style: {
                          height: 200,
                          overflowY: "scroll",
                        },
                      },
                    }}
                  >
                    <MenuItem value="">
                      <em>None</em>
                    </MenuItem>
                    {categories?.map((category) => (
                      <MenuItem key={category?.id} value={category?.id}>
                        {category?.categoryName}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
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
                  Are you sure to delete category:{" "}
                  {categories.map((category) => {
                    if (category.id === openDelete) {
                      return category.categoryName;
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

export default ProfileAdminCategories;
