import * as Yup from "yup";

const registerValidationSchema = Yup.object({
  email: Yup.string()
    .email("Invalid email format")
    .required("Email is mandatory"),
  firstname: Yup.string().required("Firstname is mandatory"),
  lastname: Yup.string().required("Lastname is mandatory"),
  password: Yup.string()
    .min(8, "Password should be 8 characters long minimum")
    .required("Password is mandatory"),
});

const loginValidationSchema = Yup.object({
  email: Yup.string()
    .email("Invalid email format")
    .required("Email is mandatory"),
  password: Yup.string()
    .min(8, "Password should be 8 characters long minimum")
    .required("Password is mandatory"),
});

const forgotPasswordValidationSchema = Yup.object({
  email: Yup.string()
    .email("Invalid email format")
    .required("Email is mandatory"),
});

const resetPasswordValidationSchema = Yup.object({
  newPassword: Yup.string()
    .min(8, "Password should be 8 characters long minimum")
    .required("Password is mandatory"),
  confirmPassword: Yup.string()
    .oneOf([Yup.ref("newPassword")], "Passwords must match")
    .required("Confirmation is mandatory"),
});

const updateProfileValidationSchema = Yup.object({
  firstname: Yup.string().required("Firstname is mandatory"),
  lastname: Yup.string().required("Lastname is mandatory"),
});

const variationOptionSchema = Yup.object({
  value: Yup.string().required("Value cannot be empty!"),
});

const variationSchema = Yup.object({
  name: Yup.string().required("Name cannot be empty!"),
});

const categorySchema = Yup.object({
  categoryName: Yup.string().required("Category name cannot be empty!"),
});

const shippingMethodSchema = Yup.object({
  name: Yup.string().required("Shipping method name cannot be empty!"),
  price: Yup.number()
    .typeError("Price must be a number")
    .required("Price is required")
    .min(0, "Price cannot be negative"),
});

const orderStatusSchema = Yup.object({
  status: Yup.string().required("Order status cannot be empty!"),
});

const productSchema = Yup.object({
  productName: Yup.string()
    .min(3, "Product name should be 3 characters long minimum")
    .required("Product name cannot be empty!"),
  description: Yup.string()
    .min(6, "Description should be 6 characters long minimum")
    .required("Description cannot be empty!"),
  categoryId: Yup.string().required("Category cannot be empty!"),
  productItems: Yup.array().of(
    Yup.object({
      price: Yup.number()
        .typeError("Price must be a number")
        .min(0, "Price cannot be negative"),
      discount: Yup.number()
        .typeError("Discount must be a number")
        .min(0, "Discount cannot be negative")
        .max(100, "Discount cannot be greater then 100%"),
      productCode: Yup.string(),
      qtyInStock: Yup.number()
        .typeError("Quantity must be a number")
        .min(0, "Quantity cannot be negative")
        .required("Quantity is required"),
    })
  ),
});

const addressSchema = Yup.object({
  country: Yup.string().required("Country cannot be empty!"),
  city: Yup.string().required("City cannot be empty!"),
  firstName: Yup.string()
    .min(2, "First name should be 2 characters long minimum")
    .required("First name cannot be empty!"),
  lastName: Yup.string()
    .min(2, "Last name should be 2 characters long minimum")
    .required("Last name cannot be empty!"),
  postalCode: Yup.string().required("Postal code cannot be empty!"),
  street: Yup.string().required("Street cannot be empty!"),
  phoneNumber: Yup.string()
    .required("Phone number cannot be empty!")
    .matches(/^[0-9]{9}$/, "Phone number must be a 9-digit number"),
});

const orderSchema = Yup.object({
  country: Yup.string().required("Country cannot be empty!"),
  city: Yup.string().required("City cannot be empty!"),
  firstName: Yup.string()
    .min(2, "First name should be 2 characters long minimum")
    .required("First name cannot be empty!"),
  lastName: Yup.string()
    .min(2, "Last name should be 2 characters long minimum")
    .required("Last name cannot be empty!"),
  postalCode: Yup.string().required("Postal code cannot be empty!"),
  street: Yup.string().required("Street cannot be empty!"),
  phoneNumber: Yup.string()
    .required("Phone number cannot be empty!")
    .matches(/^[0-9]{9}$/, "Phone number must be a 9-digit number"),
  shippingAddress: Yup.number().required("Shipping address is required!"),
  paymentMethod: Yup.number().required("Payment method is required!"),
});

const discountValidation = Yup.object({
  name: Yup.string()
    .required("Discount name is required")
    .min(3, "Name must be at least 3 characters"),
  code: Yup.string()
    .required("Discount code is required")
    .min(4, "Code must be at least 4 characters"),
  expiryDate: Yup.date()
    .required("Expiry date is required")
    .typeError("Please enter a valid date"),
  discount: Yup.number()
    .required("Discount percentage is required")
    .min(1, "Discount must be at least 1%")
    .max(100, "Discount cannot exceed 100%"),
});

const emailValidationSchema = Yup.object({
  email: Yup.string()
    .email("Invalid email format")
    .required("Email is mandatory"),
});

const updateUserProfileValidationSchema = Yup.object({
  firstname: Yup.string().required("Firstname is mandatory"),
  lastname: Yup.string().required("Lastname is mandatory"),
  newPassword: Yup.string().min(8, "Password must be at leasts 8 characters"),
});

export {
  productSchema,
  categorySchema,
  shippingMethodSchema,
  orderStatusSchema,
  registerValidationSchema,
  loginValidationSchema,
  forgotPasswordValidationSchema,
  resetPasswordValidationSchema,
  updateProfileValidationSchema,
  variationOptionSchema,
  variationSchema,
  addressSchema,
  orderSchema,
  discountValidation,
  emailValidationSchema,
  updateUserProfileValidationSchema,
};
