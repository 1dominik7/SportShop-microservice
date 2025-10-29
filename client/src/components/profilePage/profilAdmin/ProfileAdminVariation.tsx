import { useEffect, useState } from "react";
import { api } from "../../../config/api";
import { VariationOption } from "../../../types/userTypes";
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
import { variationSchema } from "../../../validator/userValidator";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { toastCustomize } from "./ProfileAdminAddProducts";
import LoadingAnimation from "../../../ui/LoadingAnimation";

interface Variation {
  id: number;
  name: string;
  variationOptions?: VariationOption[];
  categoryName?: string;
}

interface Category {
  id: number;
  categoryName: string;
  parentCategoryId: number | null;
  variationIds: number[];
}

const ProfileAdminVariation = () => {
  const [variations, setVariations] = useState<Variation[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [refresh, setRefresh] = useState<boolean>(false);
  const [editActive, setEditActive] = useState<number | null>(null);
  const [openDelete, setOpenDelete] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  useEffect(() => {
    const getCategories = async () => {
      try {
        const res = await api.get("/api/v1/category/all");
        setCategories(res.data);
      } catch (error) {
        console.error(error);
      }
    };
    getCategories();
  }, [refresh]);

  useEffect(() => {
    const getVariation = async () => {
      try {
        setIsLoading(true);
        const res = await api.get("/api/v1/variation");
        setVariations(res.data);
      } catch (error) {
        console.error(error);
      } finally {
        setIsLoading(false);
      }
    };
    getVariation();
  }, [refresh]);

  useEffect(() => {
    if (editActive !== null) {
      const variation = variations.find(
        (variation) => variation.id === editActive
      );
      const category = categories.find((category) =>
        category.variationIds?.includes(editActive)
      );

      if (variation) {
        editFormik.setFieldValue("id", variation.id);
        editFormik.setFieldValue("name", variation.name);
      }

      if (category) {
        editFormik.setFieldValue("categoryId", category.id);
      } else {
        editFormik.setFieldValue("categoryId", null);
      }
    }
  }, [editActive, variations, categories]);

  const addVariation = async (values: {
    name: string;
    categoryId: number | null;
  }) => {
    try {
      await api.post("/api/v1/variation", values);
      setRefresh((prev) => !prev);
      toast.success("Variation added successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while adding variation.",
        toastCustomize
      );
    }
  };

  const formik = useFormik({
    initialValues: {
      name: "",
      categoryId: null,
    },
    validationSchema: variationSchema,
    onSubmit: (values: { name: string; categoryId: number | null }) => {
      addVariation(values);
      formik.resetForm();
    },
  });

  const editVariation = async (
    id: number | null,
    values: {
      name: string | undefined;
      categoryId: number | null;
    }
  ) => {
    try {
      await api.put(`/api/v1/variation/${id}`, values);
      setRefresh((prev) => !prev);
      toast.info("Variation edited successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while adding variation.",
        toastCustomize
      );
    }
  };

  const editFormik = useFormik({
    initialValues: {
      id: editActive,
      name: editActive
        ? variations.find((variation) => variation.id === editActive)?.name
        : "",
      categoryId: editActive
        ? categories?.find((category) => {
            return category?.variationIds?.some(
              (variationId) => variationId === editActive
            );
          })?.id ?? null
        : null,
    },
    validationSchema: variationSchema,
    onSubmit: (values) => {
      if (values.id !== null && values.id !== undefined) {
        editVariation(values.id, {
          name: values.name,
          categoryId: values.categoryId,
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
      await api.delete(`/api/v1/variation/${id}`);
      setRefresh((prev) => !prev);
      setOpenDelete(null);
    } catch (error) {
      console.error(error);
    }
  };

  const groupedVariations = variations.reduce<Record<string, Variation[]>>(
    (acc, variation) => {
      const category = variation.categoryName || "Uncategorized";
      if (!acc[category]) {
        acc[category] = [];
      }
      acc[category].push(variation);
      return acc;
    },
    {}
  );

  return (
    <div className="h-full w-[100%] relative flex flex-col gap-4 p-6">
      {isLoading ? (
        <div className="flex items-center justify-center w-full h-full z-50 bg-white opacity-80">
          <LoadingAnimation />
        </div>
      ) : (
        <>
          <span className="text-3xl font-bold max-md:text-xl">Variation</span>
          <div className="flex flex-col gap-4 max-md:flex-col-reverse">
            {Object.entries(groupedVariations)
              .sort(([a], [b]) => a.localeCompare(b))
              .map(([categoryName, variations]) => (
                <div key={categoryName} className="pb-6 border-b-[1px] border-gray-100">
                  <div className="flex gap-2 mb-2">
                  <span>Category: </span>
                  <h2 className="text-lg font-bold capitalize">{categoryName}</h2>
                  </div>
                  <div className="flex flex-wrap gap-4 items-center">
                    {variations.map((variation) => (
                      <div
                        key={variation.id}
                        className="flex gap-2 items-center bg-gray-200 rounded-xl p-2 max-md:rounded-md"
                      >
                        {editActive === variation?.id ? (
                          <form
                            className="flex items-center"
                            onSubmit={editFormik.handleSubmit}
                          >
                            <div className="flex items-start gap-2 px-4 max-md:text-sm max-md:flex-col">
                              <TextField
                                className="w-[160px] max-md:w-full"
                                name="name"
                                label="name"
                                value={editFormik.values.name || variation.name}
                                onChange={editFormik.handleChange}
                                onBlur={editFormik.handleBlur}
                                error={
                                  editFormik.touched.name &&
                                  Boolean(editFormik.errors.name)
                                }
                                helperText={
                                  editFormik.touched.name &&
                                  editFormik.errors.name
                                }
                              />
                              <FormControl className="w-[160px] max-md:w-full">
                                <InputLabel id="selector-label">
                                  Select Category
                                </InputLabel>
                                <Select
                                  labelId="selector-label"
                                  name="categoryId"
                                  value={editFormik.values.categoryId ?? ""}
                                  onChange={editFormik.handleChange}
                                  onBlur={editFormik.handleBlur}
                                  label="Select Category"
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
                                className="h-[55px] ml-2 px-6 bg-gray-200 rounded-xl border-[1px] border-black hover:opacity-80 font-bold max-md:w-full max-md:ml-0"
                                type="submit"
                              >
                                Submit
                              </button>
                              <button
                                className="h-[55px] ml-2 px-6 bg-gray-200 rounded-xl border-[1px] border-black hover:opacity-80 font-bold max-md:w-full max-md:ml-0"
                                onClick={() => setEditActive(null)}
                              >
                                Cancel
                              </button>
                            </div>
                          </form>
                        ) : (
                          <div className="flex-col">
                            <div className="flex">
                              <div className="flex gap-[2px] px-2">
                                <span>Variation:</span>
                                <span className="font-semibold">
                                  {variation.name}
                                </span>
                              </div>
                              <EditNoteIcon
                                className="cursor-pointer hover:opacity-50"
                                onClick={() => setEditActive(variation.id)}
                              />
                              <DeleteIcon
                                className="text-red-500 cursor-pointer hover:opacity-50"
                                onClick={() => setOpenDelete(variation.id)}
                              />
                            </div>
                            {variation.categoryName && (
                              <div className="flex gap-[2px] px-2">
                                <span>Category name:</span>
                                <span className="font-semibold">
                                  {variation.categoryName}
                                </span>
                              </div>
                            )}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                </div>  
              ))}
            <span className="font-bold">Add Variation</span>
            <form className="flex items-center" onSubmit={formik.handleSubmit}>
              <div className="flex items-start gap-4 flex-wrap">
                <TextField
                  style={{ width: "300px" }}
                  name="name"
                  label="name"
                  value={formik.values.name ?? ""}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  error={formik.touched.name && Boolean(formik.errors.name)}
                  helperText={formik.touched.name && formik.errors.name}
                />
                <FormControl
                  margin="normal"
                  style={{ width: "300px", marginTop: 0 }}
                >
                  <InputLabel id="selector-label">Select Category</InputLabel>
                  <Select
                    labelId="selector-label"
                    name="categoryId"
                    value={formik.values.categoryId ?? ""}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    label="Select Category"
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
            <div className="absolute w-full h-full flex items-center justify-center top-0 left-0 ">
              <div className="absolute opacity-90 w-full h-full bg-white"></div>
              <div className="flex z-20 flex-col gap-4 items-center justify-center text-lg p-10 border-2 border-gray-200 rounded-xl bg-gray-100">
                <span className="font-bold">
                  Are you sure to delete variation option:{" "}
                  {variations.map((variation) => {
                    if (variation.id === openDelete) {
                      return variation.name;
                    }
                    return null;
                  })}
                </span>
                <div className="flex gap-4">
                  <div
                    onClick={() => deleteVariationOption(openDelete)}
                    className="w-[100px] text-center py-2 rounded-xl bg-gray-300 cursor-pointer text-red-500 font-bold hover:opacity-80"
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

export default ProfileAdminVariation;
