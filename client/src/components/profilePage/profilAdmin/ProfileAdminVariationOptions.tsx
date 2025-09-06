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
import { variationOptionSchema } from "../../../validator/userValidator";
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

const ProfileAdminVariationOptions = () => {
  const [variationOption, setVariationOption] = useState<VariationOption[]>([]);
  const [variations, setVariations] = useState<Variation[]>([]);
  const [refresh, setRefresh] = useState<boolean>(false);
  const [editActive, setEditActive] = useState<number | null>(null);
  const [openDelete, setOpenDelete] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  useEffect(() => {
    const getVariationOption = async () => {
      try {
        setIsLoading(true);
        const res = await api.get("/api/v1/variation-option");
        setVariationOption(res.data);
      } catch (error) {
        console.error(error);
      } finally {
        setIsLoading(false);
      }
    };
    getVariationOption();
  }, [refresh]);

  useEffect(() => {
    const getVariation = async () => {
      try {
        const res = await api.get("/api/v1/variation");
        setVariations(res.data);
      } catch (error) {
        console.error(error);
      }
    };
    getVariation();
  }, [refresh]);
  
  useEffect(() => {
    if (editActive !== null) {
      const option = variationOption.find((option) => option.id === editActive);
      const variation = variations.find((variation) =>
        variation?.variationOptions?.some(
          (vOption) => vOption.id === editActive
        )
      );

      if (option) {
        editFormik.setFieldValue("id", option.id);
        editFormik.setFieldValue("value", option.value);
      }

      if (variation) {
        editFormik.setFieldValue("variationId", variation.id);
      }
    }
  }, [editActive, variationOption, variations]);

  const addVariationOption = async (values: {
    value: string;
    variationId: number | null;
  }) => {
    try {
      await api.post("/api/v1/variation-option", values);
      setRefresh((prev) => !prev);
      toast.success("Variation option added successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while adding variation option.",
        toastCustomize
      );
    }
  };

  const formik = useFormik({
    initialValues: {
      value: "",
      variationId: null,
    },
    validationSchema: variationOptionSchema,
    onSubmit: (values: { value: string; variationId: number | null }) => {
      addVariationOption(values);
      formik.resetForm();
    },
  });
  const editVariationOption = async (
    id: number | null,
    values: {
      value: string | undefined;
      variationId: number | null;
    }
  ) => {
    try {
      await api.put(`/api/v1/variation-option/${id}`, values);
      setRefresh((prev) => !prev);
      toast.info("Variation option edited successfully!", toastCustomize);
    } catch (error) {
      console.error(error);
      toast.error(
        "Something went wrong while editing variation option.",
        toastCustomize
      );
    }
  };

  const editFormik = useFormik({
    initialValues: {
      id: editActive,
      value: editActive
        ? variationOption.find((option) => option.id === editActive)?.value
        : "",
      variationId: editActive
        ? variations?.find((variation) => {
            return variation?.variationOptions?.some(
              (vOption) => vOption?.id === editActive
            );
          })?.id ?? null
        : null,
    },
    validationSchema: variationOptionSchema,
    onSubmit: (values) => {
      console.log(values);
      if (values.id !== null && values.id !== undefined) {
        editVariationOption(values.id, {
          value: values.value,
          variationId: values.variationId,
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
      await api.delete(`/api/v1/variation-option/${id}`);
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
          <span className="text-3xl font-bold max-md:text-xl">Variation Options</span>
          <div className="flex flex-col gap-6 max-md:flex-col-reverse">
            <div className="flex flex-wrap gap-4 items-center">
              {variationOption.map((option) => (
                <div
                  key={option.id}
                  className="flex gap-2 items-center bg-gray-200 rounded-xl p-2 max-md:rounded-md"
                >
                  {editActive === option?.id ? (
                    <form
                      className="flex items-center w-full"
                      onSubmit={editFormik.handleSubmit}
                    >
                      <div className="flex items-start gap-2 px-4 max-md:text-sm max-md:flex-col">
                        <TextField
                       className="w-[160px] max-md:w-full"
                          name="value"
                          label="value"
                          value={editFormik.values.value || option.value}
                          onChange={editFormik.handleChange}
                          onBlur={editFormik.handleBlur}
                          error={
                            editFormik.touched.value &&
                            Boolean(editFormik.errors.value)
                          }
                          helperText={
                            editFormik.touched.value && editFormik.errors.value
                          }
                        />
                        <FormControl
                       className="w-[160px] max-md:w-full"
                        >
                          <InputLabel id="selector-label">
                            Select Variation
                          </InputLabel>
                          <Select
                            labelId="selector-label"
                            name="variationId"
                            value={editFormik.values.variationId ?? ""}
                            onChange={editFormik.handleChange}
                            onBlur={editFormik.handleBlur}
                            label="Select Option"
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
                            {[...variations]
                              .sort((a, b) => {
                                const categoryCompare = (
                                  a.categoryName || ""
                                ).localeCompare(b.categoryName || "");
                                return categoryCompare !== 0
                                  ? categoryCompare
                                  : a.name.localeCompare(b.name);
                              })
                              .map((variation) => (
                                <MenuItem
                                  key={variation.id}
                                  value={variation.id}
                                >
                                  {variation.categoryName}: {variation.name}
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
                    <div className="flex gap-2 px-2">
                      <div className="flex gap-[1px]">
                        <span>Variation option:</span>
                        <span className="font-semibold">{option.value}</span>
                      </div>
                      <div className="flex gap-[2px]">
                        <EditNoteIcon
                          className="cursor-pointer hover:opacity-50"
                          onClick={() => setEditActive(option.id)}
                        />
                        <DeleteIcon
                          className="text-red-500 cursor-pointer hover:opacity-50"
                          onClick={() => setOpenDelete(option.id)}
                        />
                      </div>
                    </div>
                  )}
                </div>
              ))}
            </div>
            <span className="font-bold">Add Variation</span>
            <form className="flex items-center" onSubmit={formik.handleSubmit}>
              <div className="flex items-start gap-4 flex-wrap">
                <TextField
                  style={{ width: "300px" }}
                  name="value"
                  label="value"
                  value={formik.values.value ?? ""}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  error={formik.touched.value && Boolean(formik.errors.value)}
                  helperText={formik.touched.value && formik.errors.value}
                />
                <FormControl
                  style={{ width: "300px", marginTop: 0 }}
                >
                  <InputLabel id="selector-label">Select Variation</InputLabel>
                  <Select
                    labelId="selector-label"
                    name="variationId"
                    value={formik.values.variationId ?? ""}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    label="Select Option"
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
                    {[...variations]
                      .sort((a, b) => {
                        const categoryCompare = (
                          a.categoryName || ""
                        ).localeCompare(b.categoryName || "");
                        return categoryCompare !== 0
                          ? categoryCompare
                          : a.name.localeCompare(b.name);
                      })
                      .map((variation) => (
                        <MenuItem key={variation.id} value={variation.id}>
                          {variation.categoryName}: {variation.name}
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
                  {variationOption.map((vOption) => {
                    if (vOption.id === openDelete) {
                      return vOption.value;
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

export default ProfileAdminVariationOptions;
