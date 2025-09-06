import { ChangeEvent, useEffect, useState } from "react";
import { api } from "../../../config/api";
import {
  Category,
  ToastOptions,
  VariationOption,
} from "../../../types/userTypes";
import { useFormik } from "formik";
import {
  Checkbox,
  FormControl,
  InputLabel,
  ListItemText,
  MenuItem,
  Select,
  SelectChangeEvent,
  TextField,
} from "@mui/material";
import { productSchema } from "../../../validator/userValidator";
import LoadingAnimation from "../../../ui/LoadingAnimation";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

interface ProductItem {
  id?: number;
  price: number;
  discount: number;
  productCode: string;
  qtyInStock: number | null;
  productImages: string[];
  variationOptionIds: number[];
}

interface Product {
  id?: number;
  productName: string;
  description: string;
  categoryId: number | null;
  productItems: ProductItem[];
}

interface Variation {
  id: number;
  name: string;
  variationOptions: VariationOption[];
}

export const toastCustomize: ToastOptions = {
  position: "bottom-right",
  autoClose: 3000,
  hideProgressBar: false,
  closeOnClick: true,
  pauseOnHover: true,
};

const ProfileAdminAddProducts = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [variations, setVariations] = useState<Variation[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const [images, setImages] = useState<{ [key: number]: File[] }>({});
  const [imagePreviews, setImagePreviews] = useState<{
    [key: number]: string[];
  }>({});
  const [assignedImages, setAssignedImages] = useState<{
    [key: number]: number[];
  }>({});
  const [fileInputs, setFileInputs] = useState<number[]>([0]);

  const addProduct = async (values: Product) => {
    setIsLoading(true);
    try {
      const uploadResults = await Promise.all(
        Object.entries(images).flatMap(([inputIndex, files]) =>
          files.map(async (file) => ({
            url: await uploadImageToCloudinary(file),
            originalInputIndex: Number(inputIndex),
          }))
        )
      );

      const urlMap = uploadResults.reduce(
        (acc, { url, originalInputIndex }) => {
          if (!acc[originalInputIndex]) acc[originalInputIndex] = [];
          acc[originalInputIndex].push(url);
          return acc;
        },
        {} as Record<number, string[]>
      );

      const updatedProductItems = values.productItems.map(
        (item, productItemIndex) => {
          const assignedInputIndices = assignedImages[productItemIndex] || [];
          const productImages = assignedInputIndices.flatMap((inputIndex) =>
            (urlMap[inputIndex] || []).map((url) => ({ imageFilename: url }))
          );

          return {
            ...item,
            productImages,
          };
        }
      );

      console.log("Final payload:", {
        ...values,
        productItems: updatedProductItems,
      });

      const response = await api.post("/api/v1/products", {
        ...values,
        productItems: updatedProductItems,
      });

      toast.success("Product added successfully!", toastCustomize);
      return response.data;
    } catch (error) {
      toast.error("Failed to add product", toastCustomize);
      console.error("Error:", error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const formik = useFormik({
    initialValues: {
      productName: "",
      description: "",
      categoryId: null,
      productItems: [
        {
          price: 0,
          discount: 0,
          productCode: "",
          qtyInStock: null,
          productImages: [] as string[],
          variationOptionIds: [],
        },
      ],
    },
    validationSchema: productSchema,
    onSubmit: async (values: Product) => {
      await addProduct(values);

      formik.resetForm();
      setImages({});
      setImagePreviews({});
      setAssignedImages({});
      setFileInputs([0]);

      formik.setFieldValue("productItems", [
        {
          price: 0,
          discount: 0,
          productCode: "",
          qtyInStock: null,
          productImages: [],
          variationOptionIds: [],
        },
      ]);
    },
  });

  const addProductItem = () => {
    const newProductItem: ProductItem = {
      price: 0,
      discount: 0,
      productCode: "",
      qtyInStock: null,
      productImages: [] as string[],
      variationOptionIds: [],
    };
    formik.setFieldValue("productItems", [
      ...formik.values.productItems,
      newProductItem,
    ]);
  };

  const addFileInput = () => {
    setFileInputs((prev) => {
      const newIndex = prev.length;
      setImagePreviews((prevPreviews) => ({
        ...prevPreviews,
        [newIndex]: [],
      }));
      return [...prev, newIndex];
    });
  };

  const removeFileInput = (inputIndex: number) => {
    setFileInputs((prev) => prev.filter((i) => i !== inputIndex));
    setImages((prev) => {
      const newImages = { ...prev };
      delete newImages[inputIndex];
      return newImages;
    });
    setImagePreviews((prev) => {
      const newPreviews = { ...prev };
      delete newPreviews[inputIndex];
      return newPreviews;
    });
  };

  const handleProductItemChange = (
    index: number,
    event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
    field: keyof ProductItem
  ) => {
    const updateProductItems = [...formik.values.productItems];
    updateProductItems[index][field] = event.target.value as never;
    formik.setFieldValue("productItems", updateProductItems);
  };

  const handleVariationOptionChange = (
    index: number,
    event: SelectChangeEvent<string | number[]>
  ) => {
    const updateProductItems = [...formik.values.productItems];

    const selectedValues = Array.isArray(event.target.value)
      ? event.target.value.map((value) => Number(value))
      : [];

    updateProductItems[index].variationOptionIds = selectedValues;

    formik.setFieldValue("productItems", updateProductItems);
  };

  const handleAssignImage = (
    imageIndex: number,
    selectedProductItems: number[]
  ) => {
    const newAssignedImages = { ...assignedImages };

    Object.keys(newAssignedImages).forEach((key) => {
      const productItemIndex = Number(key);
      newAssignedImages[productItemIndex] = newAssignedImages[
        productItemIndex
      ].filter((idx) => idx !== imageIndex);
    });

    selectedProductItems.forEach((productItemIndex) => {
      if (!newAssignedImages[productItemIndex]) {
        newAssignedImages[productItemIndex] = [];
      }

      if (!newAssignedImages[productItemIndex].includes(imageIndex)) {
        newAssignedImages[productItemIndex].push(imageIndex);
      }
    });
    console.log("Updated assignments:", newAssignedImages);
    setAssignedImages(newAssignedImages);
  };

  const handleImageUpload = (
    inputIndex: number,
    event: ChangeEvent<HTMLInputElement>
  ) => {
    const files = event.target.files;
    if (files && files.length > 0) {
      const newFiles: File[] = Array.from(files);
      const newPreviews: string[] = newFiles.map((file) =>
        URL.createObjectURL(file)
      );

      setImages((prev) => ({
        ...prev,
        [inputIndex]: [...(prev[inputIndex] || []), ...newFiles],
      }));

      setImagePreviews((prev) => ({
        ...prev,
        [inputIndex]: [...(prev[inputIndex] || []), ...newPreviews],
      }));
    }
  };

  const uploadImageToCloudinary = async (file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("upload_preset", "ecommerce-new");
    formData.append("folder", "ecommerce");

    try {
      const response = await fetch(
        "https://api.cloudinary.com/v1_1/dominikdev/image/upload",
        {
          method: "POST",
          body: formData,
        }
      );

      if (!response.ok) {
        throw new Error("Failed to upload image");
      }

      const data = await response.json();
      return data.secure_url;
    } catch (error) {
      console.error("Error uploading image to Cloudinary:", error);
      return null;
    }
  };

  const handleDeleteProductItem = (index: number) => {
    const updatedProductItems = [...formik.values.productItems];
    updatedProductItems.splice(index, 1);
    formik.setFieldValue("productItems", updatedProductItems);
  };

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
  }, []);

  useEffect(() => {
    const getVariationsByCategory = async () => {
      if (formik.values.categoryId) {
        try {
          const res = await api.get(
            `/api/v1/variation/byCategory/${formik.values.categoryId}`
          );

          const sizeOrder = [
            "xs",
            "s",
            "m",
            "l",
            "xl",
            "xxl",
            "26",
            "27",
            "28",
            "29",
            "30",
            "31",
            "32",
            "33",
            "34",
            "35",
            "36",
            "37",
            "38",
            "39",
            "40",
            "41",
            "42",
            "43",
            "44",
            "45",
            "46",
            "47",
            "48",
            "49",
          ];

          const sortedVariations = res.data.map((variation: Variation) => ({
            ...variation,
            variationOptions: [...variation.variationOptions].sort((a, b) => {
              if (variation.name.toLowerCase() === "size") {
                const getSizeIndex = (value: string) => {
                  const lowerValue = value.toLowerCase();
                  return sizeOrder.findIndex((size) =>
                    lowerValue.includes(size)
                  );
                };
                return getSizeIndex(a.value) - getSizeIndex(b.value);
              }
              return a.value.localeCompare(b.value);
            }),
          }));

          setVariations(sortedVariations);
        } catch (error) {
          console.error(error);
        }
      }
    };
    getVariationsByCategory();
  }, [formik.values.categoryId]);

  return (
    <div className="w-full h-full flex flex-col gap-4 p-6">
      {isLoading ? (
        <div className="fixed top-0 left-0 flex h-full w-full z-50 bg-white opacity-80 items-center justify-center">
          <LoadingAnimation />
        </div>
      ) : (
        <>
          <h1 className="font-bold text-3xl max-md:text-xl">Add Product</h1>
          <form
            className="flex items-center"
            onSubmit={(e) => {
              e.preventDefault();
              formik.handleSubmit();
            }}
          >
            <div className="w-[400px] h-full flex flex-col items-start gap-4 max-md:max-full">
              <TextField
                fullWidth
                name="productName"
                label="Product Name"
                value={formik.values.productName ?? ""}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                error={
                  formik.touched.productName &&
                  Boolean(formik.errors.productName)
                }
                helperText={
                  formik.touched.productName && formik.errors.productName
                }
              />
              <TextField
                        fullWidth
                name="description"
                label="Description"
                value={formik.values.description ?? ""}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                error={
                  formik.touched.description &&
                  Boolean(formik.errors.description)
                }
                helperText={
                  formik.touched.description && formik.errors.description
                }
                multiline
                rows={5}
              />
              <FormControl
                margin="normal"
                fullWidth
              >
                <InputLabel id="selector-label">Select Category</InputLabel>
                <Select
                  labelId="selector-label"
                  name="categoryId"
                  value={formik.values.categoryId ?? ""}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  label="Select Category"
                  error={
                    formik.touched.categoryId &&
                    Boolean(formik.errors.categoryId)
                  }
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
                className="h-[55px] px-6 bg-green-500 rounded-xl hover:opacity-80 font-bold text-white hover:bg-green-700"
                type="submit"
              >
                Add Product
              </button>
            </div>
          </form>
          <div className="flex flex-col gap-6">
            <span className="font-bold text-xl">Add Product Item</span>
            <div className="flex flex-col gap-6">
              {formik.values.productItems.map((item, index) => (
                <div
                  className="w-full h-full flex flex-col flex-wrap items-start gap-4"
                  key={index}
                >
                  <span className="font-semibold">
                    Product Item: {index + 1}
                  </span>
                  <div className="w-[300px] flex flex-wrap gap-4 max-sm:w-full">
                    <TextField
                            fullWidth
                      name={`productItems[${index}].price`}
                      label="Price"
                      value={item.price ?? 0}
                      onChange={(e) =>
                        handleProductItemChange(index, e, "price")
                      }
                      onBlur={formik.handleBlur}
                      error={Boolean(
                        (
                          formik.touched.productItems?.[index] as {
                            price?: boolean;
                          }
                        )?.price &&
                          (
                            formik.errors.productItems?.[index] as {
                              price?: string;
                            }
                          )?.price
                      )}
                      helperText={
                        (
                          formik.touched.productItems?.[index] as {
                            price?: boolean;
                          }
                        )?.price &&
                        (
                          formik.errors.productItems?.[index] as {
                            price?: string;
                          }
                        )?.price
                      }
                    />
                    <TextField
                  fullWidth
                      name={`productItems[${index}].discount`}
                      label="Discount"
                      value={item.discount ?? 0}
                      onChange={(e) =>
                        handleProductItemChange(index, e, "discount")
                      }
                      onBlur={formik.handleBlur}
                      error={Boolean(
                        (
                          formik.touched.productItems?.[index] as {
                            discount?: boolean;
                          }
                        )?.discount &&
                          (
                            formik.errors.productItems?.[index] as {
                              discount?: string;
                            }
                          )?.discount
                      )}
                      helperText={
                        (
                          formik.touched.productItems?.[index] as {
                            discount?: boolean;
                          }
                        )?.discount &&
                        (
                          formik.errors.productItems?.[index] as {
                            discount?: string;
                          }
                        )?.discount
                      }
                    />
                    <TextField
                   fullWidth
                      name={`productItems[${index}].productCode`}
                      label="Product Code"
                      value={item.productCode ?? ""}
                      onChange={(e) =>
                        handleProductItemChange(index, e, "productCode")
                      }
                      onBlur={formik.handleBlur}
                      error={Boolean(
                        (
                          formik.touched.productItems?.[index] as {
                            productCode?: boolean;
                          }
                        )?.productCode &&
                          (
                            formik.errors.productItems?.[index] as {
                              productCode?: string;
                            }
                          )?.productCode
                      )}
                      helperText={
                        (
                          formik.touched.productItems?.[index] as {
                            productCode?: boolean;
                          }
                        )?.productCode &&
                        (
                          formik.errors.productItems?.[index] as {
                            productCode?: string;
                          }
                        )?.productCode
                      }
                    />
                    <TextField
                          fullWidth
                      name={`productItems[${index}].qtyInStock`}
                      label="Quantity"
                      value={item.qtyInStock ?? 0}
                      onChange={(e) =>
                        handleProductItemChange(index, e, "qtyInStock")
                      }
                      onBlur={formik.handleBlur}
                      error={Boolean(
                        (
                          formik.touched.productItems?.[index] as {
                            qtyInStock?: boolean;
                          }
                        )?.qtyInStock &&
                          (
                            formik.errors.productItems?.[index] as {
                              qtyInStock?: string;
                            }
                          )?.qtyInStock
                      )}
                      helperText={
                        (
                          formik.touched.productItems?.[index] as {
                            qtyInStock?: boolean;
                          }
                        )?.qtyInStock &&
                        (
                          formik.errors.productItems?.[index] as {
                            qtyInStock?: string;
                          }
                        )?.qtyInStock
                      }
                    />
                  </div>
                  <div className="flex flex-col gap-2 flex-wrap max-lg:w-[450px] max-md:w-full">
                    <FormControl
                      margin="normal"
                      fullWidth
                    >
                      <InputLabel id="selector-label">
                        Select Variation Option
                      </InputLabel>
                      <Select
                      fullWidth
                        labelId="selector-label"
                        name={`productItems[${index}].variationOptionIds`}
                        value={item.variationOptionIds || []}
                        multiple
                        onChange={(e: SelectChangeEvent<string | number[]>) => {
                          handleVariationOptionChange(index, e);
                        }}
                        onBlur={formik.handleBlur}
                        label="Select Variation Options"
                        MenuProps={{
                          PaperProps: {
                            style: {
                              height: 300,
                              overflowY: "scroll",
                            },
                          },
                        }}
                      >
                        <MenuItem value="">
                          <em>None</em>
                        </MenuItem>
                        {variations?.map((variation) =>
                          variation.variationOptions?.map((vOption) => (
                            <MenuItem key={vOption?.id} value={vOption?.id}>
                              {variation.name}: {vOption?.value}
                            </MenuItem>
                          ))
                        )}
                      </Select>
                    </FormControl>
                    {
                      <div className="flex flex-col gap-2">
                        <strong>Selected Options:</strong>
                        <div className="flex flex-wrap gap-2">
                          {item.variationOptionIds.map((selectedId) => {
                            const selectedOption = variations
                              .map((variation) =>
                                variation.variationOptions.find(
                                  (vOption) => vOption.id === selectedId
                                )
                              )
                              .find((option) => option !== undefined);
                            return selectedOption ? (
                              <span
                                key={selectedId}
                                className="bg-gray-200 px-2 py-1 rounded-lg text-sm"
                              >
                                {selectedOption.value}
                              </span>
                            ) : null;
                          })}
                        </div>
                      </div>
                    }
                  </div>
                  <button
                    type="button"
                    className="bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-700"
                    onClick={() => handleDeleteProductItem(index)}
                  >
                    Delete Product Item
                  </button>
                </div>
              ))}
              <button
                className="h-[55px] w-max px-6 bg-gray-200 rounded-xl border-[1px] border-black hover:opacity-80 font-bold"
                onClick={() => addProductItem()}
              >
                Add Product Item
              </button>
              <div className="flex flex-col gap-4 max-md:w-full">
                <div className="max-md:w-full">
                  {fileInputs.map((inputIndex) => (
                    <div
                      key={inputIndex}
                      className="flex items-center gap-2 mb-2 max-md:flex-col"
                    >
                      <input
                        type="file"
                        multiple
                        onChange={(e) => handleImageUpload(inputIndex, e)}
                        className="border rounded-lg p-2"
                      />
                      <button
                        type="button"
                        onClick={() => removeFileInput(inputIndex)}
                        className="bg-red-500 text-white px-3 py-1 rounded-lg hover:bg-red-700"
                      >
                        Remove
                      </button>
                    </div>
                  ))}
                  <button
                    type="button"
                    onClick={addFileInput}
                    className="bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-700"
                  >
                    Add More Images
                  </button>
                </div>
                <div className="flex flex-wrap gap-4">
                  {fileInputs.map((inputIndex) => (
                    <div key={inputIndex} className="flex flex-col gap-2 max-md:w-full">
                      <div className="flex gap-2 flex-wrap">
                        {imagePreviews[inputIndex]?.map((url, index) => (
                          <div key={index} className="flex flex-col gap-2">
                            <img
                              src={url}
                              alt="Preview"
                              className="h-[100px] w-[100px] object-cover"
                            />
                          </div>
                        ))}
                      </div>
                      <FormControl
                        margin="normal"
                        fullWidth
                      >
                        <InputLabel id={`assign-label-${inputIndex}`} >
                          Assign to Product Item
                        </InputLabel>
                        <Select
                          labelId={`assign-label-${inputIndex}`}
                          label="Assign To Product Item"
                          multiple
                          value={Object.entries(assignedImages)
                            .filter(([_, imgIndices]) =>
                              imgIndices.includes(inputIndex)
                            )
                            .map(([productItemIndex]) =>
                              Number(productItemIndex)
                            )}
                          onChange={(e) =>
                            handleAssignImage(
                              inputIndex,
                              e.target.value as number[]
                            )
                          }
                          renderValue={(selected) =>
                            (selected as number[])
                              .map((idx) => `Product Item ${idx + 1}`)
                              .join(", ")
                          }
                          MenuProps={{
                            PaperProps: {
                              style: {
                                height: 200,
                                overflowY: "scroll",
                              },
                            },
                          }}
                        >
                          {formik.values.productItems.map(
                            (_, productItemIndex) => (
                              <MenuItem
                                key={productItemIndex}
                                value={productItemIndex}
                              >
                                <Checkbox
                                  checked={
                                    assignedImages[productItemIndex]?.includes(
                                      inputIndex
                                    ) || false
                                  }
                                />
                                <ListItemText
                                  primary={`Product Item ${
                                    productItemIndex + 1
                                  }`}
                                />
                              </MenuItem>
                            )
                          )}
                        </Select>
                      </FormControl>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </>
      )}
      <ToastContainer />
    </div>
  );
};

export default ProfileAdminAddProducts;
