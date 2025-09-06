import { ChangeEvent, useEffect, useState } from "react";
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
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { Category, VariationOption } from "../../types/userTypes";
import { api } from "../../config/api";
import { productSchema } from "../../validator/userValidator";
import LoadingAnimation from "../../ui/LoadingAnimation";
import { toastCustomize } from "../profilePage/profilAdmin/ProfileAdminAddProducts";
import { useProductById } from "../../hooks/query";
import { useNavigate, useParams } from "react-router";
import DeleteForeverIcon from "@mui/icons-material/DeleteForever";
import { useMutation } from "@tanstack/react-query";

export interface EditProduct {
  id?: number;
  productName: string;
  description: string;
  categoryId: number | null;
  productItems: ProductItem[];
}

interface ProductItem {
  id?: number;
  price: number;
  discount: number;
  productCode: string;
  qtyInStock: number | null;
  productImages: ProductImage[];
  variationOptionIds: number[];
}

interface ProductImage {
  id?: number;
  imageFilename: string;
}

interface Variation {
  id: number;
  name: string;
  variationOptions: VariationOption[];
}

const EditProduct = () => {
  const navigate = useNavigate();
  const { productId } = useParams();
  const productIdNumber = Number(productId);

  const [categories, setCategories] = useState<Category[]>([]);
  const [variations, setVariations] = useState<Variation[]>([]);

  const [allImages, setAllImages] = useState<ProductImage[]>([]);
  const [images, setImages] = useState<{ [key: number]: File[] }>({});
  const [imagePreviews, setImagePreviews] = useState<{
    [key: number]: ProductImage[];
  }>({});
  const [fileInputs, setFileInputs] = useState<number[]>([0]);
  const [assignedImages, setAssignedImages] = useState<{
    [key: number]: number[];
  }>({});

  const [openDeleteProduct, setOpenDeleteProduct] = useState<boolean>(false);

  const { data, isLoading, isFetching } = useProductById(Number(productId));

  useEffect(() => {
    if (data) {
      const uniqueImages: ProductImage[] = [];
      const imageMap: { [key: string]: ProductImage } = {};

      data.productItems.forEach((item) => {
        item.productImages.forEach((image) => {
          if (!imageMap[image.imageFilename]) {
            imageMap[image.imageFilename] = image;
            uniqueImages.push(image);
          }
        });
      });

      setAllImages(uniqueImages);

      const initialAssignedImages: { [key: number]: number[] } = {};
      data.productItems.forEach((item, productItemIndex) => {
        initialAssignedImages[productItemIndex] = item.productImages.map(
          (image) =>
            uniqueImages.findIndex(
              (img) => img.imageFilename === image.imageFilename
            )
        );
      });

      setAssignedImages(initialAssignedImages);
    }
  }, [data]);

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

  const mutation = useMutation({
    mutationFn: async (data: { values: EditProduct; productId: number }) => {
      const res = await api.put(
        `/api/v1/products/${data.productId}`,
        data.values
      );
      return res.data;
    },
    onSuccess: () => {
      navigate("/profile/adminPanel/products");
      setTimeout(() => {
        toast.success("Product updated successfully!", toastCustomize);
      }, 100);
    },
    onError: (error: Error) => {
      toast.error("Error updating product.", toastCustomize);
      console.error("Error:", error);
    },
  });

  const updateProduct = async (values: EditProduct) => {
    try {
      const cloudinaryUrls = (
        await Promise.all(
          Object.values(images)
            .flat()
            .map(async (file) => {
              const url = await uploadImageToCloudinary(file);
              console.log("Uploaded URL:", url);
              return url;
            })
        )
      ).filter((url) => url !== null && url !== undefined);

      const updatedProductItems = values.productItems.map(
        (item, productItemIndex) => {
          const assignedImageUrls = (assignedImages[productItemIndex] || [])
            .map((imageIndex) => cloudinaryUrls[imageIndex])
            .filter((url) => url !== undefined && url !== null);

          const existingImages = (item.productImages || []).filter(
            (image) => image?.id
          );

          const newImages = assignedImageUrls.map((url) => ({
            imageFilename: url,
          }));

          return {
            ...item,
            productImages: [...existingImages, ...newImages],
          };
        }
      );

      const productWithImages = {
        ...values,
        productItems: updatedProductItems,
      };

      await mutation.mutateAsync({
        values: productWithImages,
        productId: productIdNumber,
      });
    } catch (error) {
      console.error("Error updating product:", error);
      toast.error(
        "Something went wrong while updating the product.",
        toastCustomize
      );
    }
  };

  const formik = useFormik({
    enableReinitialize: false,
    initialValues: {
      productName: data?.productName ?? "",
      description: data?.description ?? "",
      categoryId: data?.categoryId ?? null,
      productItems: (data?.productItems ?? []).map((item) => ({
        id: item.id ?? undefined,
        price: item.price ?? 0,
        discount: item.discount ?? 0,
        productCode: item.productCode ?? "",
        qtyInStock: item.qtyInStock ?? 0,
        productImages:
          item.productImages?.map((image) => ({
            id: image.id,
            imageFilename: image.imageFilename,
          })) ?? [],
        variationOptionIds: item.variationOptionIds ?? [],
      })),
    },
    validationSchema: productSchema,
    onSubmit: async (values: EditProduct) => {
      await updateProduct(values);

      formik.resetForm();

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
      productImages: [] as ProductImage[],
      variationOptionIds: [],
    };

    const updatedProductItems = [...formik.values.productItems, newProductItem];
    formik.setFieldValue("productItems", updatedProductItems);
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
    const updatedProductItems = [...formik.values.productItems];

    updatedProductItems[index] = {
      ...updatedProductItems[index],
      [field]: event.target.value,
    };

    formik.setFieldValue("productItems", updatedProductItems);
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
    const newAssignedImages: { [key: number]: number[] } = {
      ...assignedImages,
    };

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

    setAssignedImages(newAssignedImages);

    const updatedProductItems = formik.values.productItems.map(
      (item, productItemIndex) => {
        const imageIds = newAssignedImages[productItemIndex] || [];
        return {
          ...item,
          productImages: imageIds.map((idx) => allImages[idx]),
        };
      }
    );

    formik.setFieldValue("productItems", updatedProductItems);
  };

  const handleImageUpload = (
    productItemIndex: number,
    event: ChangeEvent<HTMLInputElement>
  ) => {
    const files = event.target.files;
    if (files && files.length > 0) {
      const newFiles: File[] = Array.from(files);

      const newPreviews: ProductImage[] = newFiles.map((file, index) => ({
        id: Date.now() + index,
        imageFilename: URL.createObjectURL(file),
      }));

      setImages((prev) => {
        const updatedImages = {
          ...prev,
          [productItemIndex]: [...(prev[productItemIndex] || []), ...newFiles],
        };

        return updatedImages;
      });

      setAllImages((prev) => [...prev, ...newPreviews]);

      setImagePreviews((prev) => ({
        ...prev,
        [productItemIndex]: [...(prev[productItemIndex] || []), ...newPreviews],
      }));

      const newImageIndices = newPreviews.map(
        (_, index) => allImages.length + index
      );
      setAssignedImages((prev) => ({
        ...prev,
        [productItemIndex]: [
          ...(prev[productItemIndex] || []),
          ...newImageIndices,
        ],
      }));
    }
  };

  useEffect(() => {
    console.log("Updated images state:", images);
  }, [images]);

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
      if (data?.secure_url) {
        return data.secure_url;
      } else {
        throw new Error("No URL returned from Cloudinary");
      }
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

  const handleImageDelete = (imageId: number | undefined) => {
    if (imageId === undefined) {
      console.error("Image ID is undefined");
      return;
    }

    const updatedAllImages = allImages.filter((image) => image.id !== imageId);
    setAllImages(updatedAllImages);

    const updatedAssignedImages: { [key: number]: number[] } = {};
    Object.keys(assignedImages).forEach((key) => {
      const productItemIndex = Number(key);
      updatedAssignedImages[productItemIndex] = assignedImages[productItemIndex]
        .filter((idx) => allImages[idx]?.id !== imageId)
        .map((idx) => (idx > imageId ? idx - 1 : idx));
    });

    setAssignedImages(updatedAssignedImages);

    const updatedProductItems = formik.values.productItems.map((item) => {
      return {
        ...item,
        productImages: item.productImages.filter(
          (image) => image.id !== imageId
        ),
      };
    });

    formik.setFieldValue("productItems", updatedProductItems);
  };

  const deleteProduct = async () => {
    try {
      await api.delete(`/api/v1/products/${productId}`);
      navigate("/profile/adminPanel/products");
      setTimeout(() => {
        toast.success("Product deleted successfully!", toastCustomize);
      }, 100);
    } catch (error) {
      console.error(error);
      toast.error("Error updating product.", toastCustomize);
    }
  };

  useEffect(() => {
    if (data) {
      formik.setValues({
        productName: data.productName,
        description: data.description,
        categoryId: data.categoryId,
        productItems: (data?.productItems ?? []).map((item) => ({
          id: item.id ?? undefined,
          price: item.price ?? 0,
          discount: item.discount ?? 0,
          productCode: item.productCode ?? "",
          qtyInStock: item.qtyInStock ?? 0,
          productImages:
            item.productImages?.map((image) => ({
              id: image.id,
              imageFilename: image.imageFilename,
            })) ?? [],
          variationOptionIds: item.variationOptionIds ?? [],
        })),
      });
    }
  }, [data]);

  useEffect(() => {}, [data, formik]);

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
    <div className="w-full h-full flex flex-col gap-4 p-6 max-md:items-center">
      {mutation.isPending || isFetching || isLoading ? (
        <div className="fixed top-0 left-0 flex h-full w-full z-50 bg-white opacity-80 items-center justify-center">
          <LoadingAnimation />
        </div>
      ) : (
        <>
          <span className="font-bold text-3xl max-md:text-xl">
            Update Product
          </span>
          <form
            className="flex items-center"
            onSubmit={(e) => {
              e.preventDefault();
              formik.handleSubmit();
            }}
          >
            <div className="w-max h-full flex flex-col items-start gap-4">
              <TextField
                className="w-[400px] max-md:w-full"
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
                className="w-[400px] max-md:w-full"
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
              <FormControl margin="normal" className="w-[400px] max-md:w-full">
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
                        height: 300,
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
              <div className="flex gap-4">
                <button
                className="h-[55px] px-6 bg-green-500 rounded-xl hover:opacity-80 font-bold text-white hover:bg-green-700"
                  type="submit"
                >
                  Update Product
                </button>
                <button
                  className="h-[55px] px-6 bg-black text-white rounded-xl border-[1px] border-gray-200 hover:opacity-80 font-bold"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate(-1);
                  }}
                >
                  Cancel
                </button>
              </div>
              <button
                className="h-[55px] px-6 bg-red-500 rounded-xl border-[1px] hover:opacity-80 font-bold text-white"
                onClick={(e) => {
                  e.preventDefault();
                  setOpenDeleteProduct(!openDeleteProduct);
                }}
              >
                Delete Product
              </button>
            </div>
          </form>
          <div className="flex flex-col gap-6 max-md:w-full">
            <span className="font-bold text-xl">Product Items</span>
            <div className="flex flex-col gap-6">
              {formik.values.productItems.map((item, index) => (
                <div
                  className="w-full h-full flex flex-col flex-wrap items-start gap-4"
                  key={index}
                >
                  <span className="font-semibold">
                    Product Item: {index + 1}
                  </span>
                  <div className="flex flex-wrap gap-4">
                    <TextField
                      className="w-[300px] max-md:w-full"
                      name={`productItems[${index}].price`}
                      label="Price"
                      value={item.price ?? 0}
                      onChange={(e) =>
                        handleProductItemChange(index, e, "price")
                      }
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
                      onBlur={formik.handleBlur}
                    />
                    <TextField
                      className="w-[300px] max-md:w-full"
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
                      className="w-[300px] max-md:w-full"
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
                      className="w-[300px] max-md:w-full"
                      type="number"
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
                  <div className="flex flex-col gap-2 max-md:w-full">
                    <FormControl
                      margin="normal"
                      className="w-[300px] max-md:w-full"
                    >
                      <InputLabel id="selector-label">
                        Select Variation Option
                      </InputLabel>
                      <Select
                        labelId="selector-label"
                        name={`productItems[${index}].variationOptionIds`}
                        value={item.variationOptionIds ?? []}
                        multiple
                        onChange={(e: SelectChangeEvent<string | number[]>) => {
                          console.log(e.target.value);
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
                          variation.variationOptions
                            ?.slice()
                            .sort((a, b) => {
                              const valueA = parseFloat(a.value);
                              const valueB = parseFloat(b.value);
                              return valueA - valueB;
                            })
                            .map((vOption) => (
                              <MenuItem key={vOption?.id} value={vOption?.id}>
                                {variation.name}: {vOption?.value}
                              </MenuItem>
                            ))
                        )}
                      </Select>
                    </FormControl>
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
            </div>
          </div>
          <div className="flex flex-col gap-4">
            <div>
              {fileInputs.map((inputIndex) => (
                <div key={inputIndex} className="flex items-center gap-2 mb-2 max-md:flex-col">
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
              {allImages.map((image, imageIndex) => (
                <div key={image.id} className="flex flex-col gap-2 max-md:w-full">
                  <img
                    src={image.imageFilename}
                    alt="Preview"
                    className="h-[100px] w-[100px] object-cover"
                  />
                  <DeleteForeverIcon
                    style={{
                      color: "red",
                      fontSize: 36,
                      cursor: "pointer",
                    }}
                    onClick={() => handleImageDelete(image?.id)}
                  />
                  <FormControl
                    margin="normal"
                    className="w-[300px] max-md:w-full"
                  >
                    <InputLabel id="selector-label">
                      Assign to Product Item
                    </InputLabel>
                    <Select
                      label="Assign To Product Item"
                      multiple
                      value={Object.keys(assignedImages)
                        .filter((key) =>
                          assignedImages[Number(key)].includes(imageIndex)
                        )
                        .map((key) => Number(key))}
                      onChange={(e) =>
                        handleAssignImage(
                          imageIndex,
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
                      {formik.values.productItems.map((_, productItemIndex) => (
                        <MenuItem
                          key={productItemIndex}
                          value={productItemIndex}
                        >
                          <Checkbox
                            checked={
                              assignedImages[productItemIndex]?.includes(
                                imageIndex
                              ) || false
                            }
                          />
                          <ListItemText
                            primary={`Product Item ${productItemIndex + 1}`}
                          />
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
      {openDeleteProduct && (
        <div className="fixed left-0 top-0 w-full h-full flex items-center justify-center text-center">
          <div
            className="absolute w-full h-full bg-white opacity-60 z-50"
            onClick={() => setOpenDeleteProduct(!openDeleteProduct)}
          ></div>
          <div className="flex flex-col gap-4 p-6 bg-white border-[2px] border-gray-300 rounded z-50 text-center">
            <span className="text-lg font-semibold">
              Are you sure you want to delete this product?
            </span>
            <div className="w-full flex justify-center gap-6">
              <button
                className="py-3 px-4 bg-red-500 hover:opacity-80 cursor-pointer rounded text-white font-bold"
                onClick={deleteProduct}
              >
                Delete
              </button>
              <button
                className="py-3 px-4 bg-black hover:opacity-80 cursor-pointer rounded text-white font-bold"
                onClick={() => setOpenDeleteProduct(!openDeleteProduct)}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
      <ToastContainer />
    </div>
  );
};

export default EditProduct;
