import { useEffect, useRef, useState } from "react";
import { useAppSelector } from "../../state/store";
import { useCartByUserId, useUserAddresses } from "../../hooks/query";
import { useFormik } from "formik";
import { addressSchema } from "../../validator/userValidator";
import {
  Address,
  PaymentType,
  ShippingMethod,
  ShoppingCartItem,
  UserPaymentMethod,
} from "../../types/userTypes";
import {
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  useMediaQuery,
  useTheme,
} from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import { api } from "../../config/api";
import { useNavigate } from "react-router";
import LoadingAnimation from "../../ui/LoadingAnimation";

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

const Delivery = () => {
  const auth = useAppSelector((store) => store?.auth);
  const userId = auth.user?.id;
  const navigate = useNavigate();

  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

  const addressRef = useRef<HTMLDivElement>(null);
  const shippingMethodsRef = useRef<HTMLDivElement>(null);
  const paymentRef = useRef<HTMLDivElement>(null);

  const [selectedAddress, setSelectedAddress] = useState<Address | null>(null);
  const [selectedShippingMethod, setSelectedShippingMethod] =
    useState<ShippingMethod | null>(null);
  const [selectedPayment, setSelectedPayment] = useState<number | null>(null);

  const [shippingMethods, setShippingMethods] = useState<ShippingMethod[] | []>(
    []
  );
  const [paymentType, setPaymentType] = useState<PaymentType[] | []>([]);
  const [userPaymentMethods, setUserPaymentMethods] = useState<
    UserPaymentMethod[] | []
  >([]);
  const [stripeImageLoaded, setStripeImageLoaded] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { data, refetch } = useUserAddresses();
  const { data: cart } = useCartByUserId();

  const formik = useFormik({
    initialValues: {
      country: selectedAddress?.country ?? "Poland",
      city: selectedAddress?.city ?? "",
      firstName: selectedAddress?.firstName ?? "",
      lastName: selectedAddress?.lastName ?? "",
      postalCode: selectedAddress?.postalCode ?? "",
      street: selectedAddress?.street ?? "",
      phoneNumber: selectedAddress?.phoneNumber ?? "",
      addressLine1: selectedAddress?.addressLine1 ?? "",
      addressLine2: selectedAddress?.addressLine2 ?? "",
    },
    validateOnBlur: true,
    validateOnMount: true,
    validateOnChange: true,
    validationSchema: addressSchema,
    onSubmit: async (values: Address) => {
      formik.resetForm();
      refetch();
    },
  });

  useEffect(() => {
    if (selectedAddress) {
      formik.setValues({
        country: selectedAddress?.country ?? "Poland",
        city: selectedAddress?.city ?? "",
        firstName: selectedAddress?.firstName ?? "",
        lastName: selectedAddress?.lastName ?? "",
        postalCode: selectedAddress?.postalCode ?? "",
        street: selectedAddress?.street ?? "",
        phoneNumber: selectedAddress?.phoneNumber ?? "",
        addressLine1: selectedAddress?.addressLine1 ?? "",
        addressLine2: selectedAddress?.addressLine2 ?? "",
      });
    } else {
      formik.resetForm();
    }
  }, [selectedAddress]);

  useEffect(() => {
    const getShippingMethods = async () => {
      try {
        const res = await api.get("/api/v1/shipping-method/all");
        setShippingMethods(res.data);
      } catch (error) {
        console.error(error);
      }
    };
    getShippingMethods();
  }, []);

  useEffect(() => {
    const getPaymentType = async () => {
      try {
        const res = await api.get("/api/v1/payment-type/all");
        setPaymentType(res.data);
      } catch (error) {
        console.error(error);
      }
    };
    getPaymentType();
  }, []);

  useEffect(() => {
    const getUserPaymentMethod = async () => {
      try {
        const res = await api.get(`/api/v1/user-payment-method`);
        setUserPaymentMethods(res.data);
      } catch (error) {
        console.error(error);
      }
    };
    getUserPaymentMethod();
  }, []);

  const getShippingIcon = (methodName: string) => {
    const name = methodName.toLowerCase();
    if (name.includes("inpost")) return "inpost.png";
    if (name.includes("dpd")) return "dpd.png";
    return "courier.jpg";
  };

  const getPaymentIcon = (methodName: string) => {
    const name = methodName.toLowerCase();
    if (name.includes("stripe")) return "stripe.png";
    if (name.includes("payu")) return "payu.png";
    return "payment.png";
  };

  const formatDateShort = (dateString: string) => {
    const date = new Date(dateString);
    const month = (date.getMonth() + 1).toString().padStart(2, "0");
    const year = date.getFullYear().toString().slice(-2);
    return `${month}/${year}`;
  };

  const formatJavaLocalDateTimeForInput = (date = new Date()): string => {
    const isoString = date.toISOString();
    return isoString.slice(0, 19);
  };

  const totalDiscount = cart?.discountCodes?.reduce(
    (sum, item) => sum + item.discount,
    0
  );

  const shoppingCartItems: ShoppingCartItem[] = cart?.shoppingCartItems || [];

  const totalPrice = () => {
    const totalItemPriceWithoutDiscount = shoppingCartItems.reduce(
      (total, cartItem) => {
        const selectedVariant =
          cartItem?.productItem?.productItemOneByColour?.find(
            (variant) => variant.id === cartItem?.productItem?.productItemId
          );

        if (!selectedVariant) return total;

        const price = selectedVariant.price;
        const discount = selectedVariant.discount;
        const quantity = cartItem?.qty;

        const discountPrice =
          discount > 0 ? price * (1 - discount / 100) : price;

        return total + discountPrice * quantity;
      },
      0
    );
    return totalItemPriceWithoutDiscount.toFixed(2);
  };

  const calculateTotalCartValue = () => {
    const totalWithoutDiscounts = shoppingCartItems.reduce(
      (total, cartItem) => {
        const selectedVariant =
          cartItem?.productItem?.productItemOneByColour?.find(
            (variant) => variant.id === cartItem?.productItem?.productItemId
          );

        if (!selectedVariant) return total;

        const price = selectedVariant.price;
        const discount = selectedVariant.discount;
        const quantity = cartItem.qty;

        const discountedPrice =
          discount > 0 ? price * (1 - discount / 100) : price;

        return total + discountedPrice * quantity;
      },
      0
    );

    const totalDiscount =
      cart?.discountCodes?.reduce((total, code) => {
        const codeDiscount = code.discount;
        return total + totalWithoutDiscounts * (codeDiscount / 100);
      }, 0) || 0;

    return (totalWithoutDiscounts - totalDiscount).toFixed(2);
  };

  const submitOrder = async () => {
    if (!cart) {
      return;
    }
    setIsSubmitting(true);
    const orderRequest = {
      userId: userId!,
      orderDate: formatJavaLocalDateTimeForInput(),
      addressRequest: {
        id: formik.dirty ? null : selectedAddress?.id,
        country: formik.values.country,
        city: formik.values.city,
        firstName: formik.values.firstName,
        lastName: formik.values.lastName,
        postalCode: formik.values.postalCode,
        street: formik.values.street,
        phoneNumber: formik.values.phoneNumber,
        addressLine1: formik.values.addressLine1,
        addressLine2: formik.values.addressLine2,
      },
      shippingMethodId: selectedShippingMethod ? selectedShippingMethod.id : 0,
      orderTotal: Number(totalPrice()),
      finalOrderTotal:
        Number(calculateTotalCartValue()) +
        (selectedShippingMethod?.price ? selectedShippingMethod?.price : 0),
      appliedDiscountValue: totalDiscount ? totalDiscount : 0,
      providerId: selectedPayment,
      cart:cart
    };

    const request = {
      orderRequest,
      // successUrl: `${window.location.origin}/payment-success`,
      // cancelUrl: `${window.location.origin}/payment-cancel`,
    };

    const selectedPaymentName = paymentType
      .find((p) => p.id === selectedPayment)
      ?.value?.toLowerCase();

    try {
      if (selectedPaymentName?.includes("stripe")) {
        const response = await api.post(
          `/api/v1/payment/stripe/checkout`,
          request
        );
        window.location.href = response.data.checkoutUrl;
      } else if (selectedPaymentName?.includes("payu")) {
        const response = await api.post(
          `/api/v1/payment/payu/checkout`,
          request
        );
        window.location.href = response.data.checkoutUrl;
      } else {
        console.warn(
          "This payment method is not supported: ",
          selectedPaymentName
        );
      }
    } catch (error) {
      console.log("Error creating order: ", error);
    } finally {
      setIsSubmitting(false);
    }
  };

//   const submitOrder = async () => {
//   if (!cart) return;
//   setIsSubmitting(true);

//   const orderRequest = {
//     userId: userId!,
//     addressRequest: {
//       country: formik.values.country,
//       city: formik.values.city,
//       firstName: formik.values.firstName,
//       lastName: formik.values.lastName,
//       postalCode: formik.values.postalCode,
//       street: formik.values.street,
//       phoneNumber: formik.values.phoneNumber,
//       addressLine1: formik.values.addressLine1,
//       addressLine2: formik.values.addressLine2,
//     },
//     shippingMethodId: selectedShippingMethod ? selectedShippingMethod.id : 0,
//     orderTotal: Number(totalPrice()),
//     finalOrderTotal: Number(calculateTotalCartValue()) + (selectedShippingMethod?.price ?? 0),
//     appliedDiscountValue: totalDiscount ?? 0,
//     providerId: selectedPayment,
//     cart: cart
//   };
//   try {
//     // 1️⃣ Tworzymy zamówienie
//     const res = await api.post(`api/v1/shop-order/create`, orderRequest);
//     console.log(res.data.id)
//     const orderId = res.data.id; // musisz mieć w ShopOrderResponse

//     // 2️⃣ Polling / sprawdzanie czy checkout URL jest gotowy
//     let checkoutUrl: string | null = null;
//     const maxRetries = 10;
//     let attempts = 0;

//     while (!checkoutUrl && attempts < maxRetries) {
//       const urlRes = await api.get(`/api/v1/shop-order/checkout-url/${orderId}`);
//       console.log(urlRes)
//       if (urlRes.status === 200 && urlRes.data.checkoutUrl) {
//         checkoutUrl = urlRes.data.checkoutUrl;
//       } else {
//         // jeśli status 202 lub brak checkoutUrl → czekamy 1s i próbujemy ponownie
//         await new Promise((resolve) => setTimeout(resolve, 3000));
//       }
//       attempts++;
//     }

//     if (checkoutUrl) {
//       window.location.href = checkoutUrl; // przekierowanie do płatności
//     } else {
//       console.error("Checkout URL not ready after several attempts");
//     }
//   } catch (error) {
//     console.log("Error creating order: ", error);
//   } finally {
//     setIsSubmitting(false);
//   }
// };

  const hasUnavailableProducts = cart?.shoppingCartItems?.some((item) => {
    const selectedVariant = item?.productItem?.productItemOneByColour?.find(
      (variant) => variant.id === item?.productItem?.productItemId
    );

    const availableQty = selectedVariant?.qtyInStock ?? 0;

    return item.qty > availableQty;
  });

  return (
    <>
      <div className="w-full h-full flex gap-6 py-6 px-16 max-md:px-4 max-md:py-4 max-md:gap-2 max-lg:flex-col">
        {cart?.shoppingCartItems !== undefined &&
        cart?.shoppingCartItems.length > 0 ? (
          <>
            <div className="w-2/3 flex overflow-y-scroll max-lg:w-full">
              <div className="w-full flex flex-col p-4 gap-10 max-md:gap-6">
                <h1 className="font-bold text-2xl max-md:text-xl">
                  1. Enter your address
                </h1>
                {data && data.length > 0 && (
                  <div className="flex flex-grow gap-4 max-md:flex-col">
                    {data.map((address) => (
                      <div
                        key={address.id}
                        onClick={() => {
                          setSelectedAddress(address);
                          setTimeout(() => {
                            shippingMethodsRef?.current?.scrollIntoView({
                              behavior: "smooth",
                              block: "start",
                            });
                          }, 500);
                        }}
                        className={`w-[300px] h-[300px] flex flex-col justify-between outline hover:outline-4 hover:outline-secondary-color p-6 cursor-pointer max-md:h-max max-md:p-2 max-md:gap-4 ${
                          selectedAddress?.id === address.id
                            ? "outline outline-4 outline-secondary-color"
                            : "outline outline-1 outline-gray-300"
                        }`}
                      >
                        <div className="flex flex-col gap-[2px]">
                          <div className="flex gap-2">
                            <span>{address?.firstName}</span>
                            <span>{address?.lastName}</span>
                          </div>
                          <div className="flex gap-2">
                            <span>{address?.street}</span>
                            <span>{address?.addressLine1}</span>
                            <span>{address?.addressLine2}</span>
                          </div>
                          <div className="flex gap-2">
                            <span>{address?.postalCode}</span>
                            <span>{address?.city}</span>
                          </div>
                          <span>{address?.phoneNumber}</span>
                          <span>{address?.country}</span>
                        </div>
                        <div className="flex gap-4 items-center">
                          <button
                            className="w-full p-3 text-center bg-gray-200 rounded-full cursor-pointer hover:bg-gray-100 font-bold text-lg max-md:text-base max-md:p-2"
                            onClick={(e) => {
                              e.stopPropagation();
                              setTimeout(() => {
                                if (addressRef.current) {
                                  window.scroll({
                                    top: addressRef.current.offsetTop - 120,
                                    behavior: "smooth",
                                  });
                                }
                              }, 500);
                            }}
                          >
                            Edit
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
                <div
                  className="flex h-full w-full items-center justify-center"
                  ref={addressRef}
                >
                  <div className="w-full flex flex-col gap-6 max-md:gap-2">
                    <FormControl margin="normal" fullWidth>
                      <InputLabel id="selector-label">
                        Select Country
                      </InputLabel>
                      <Select
                        labelId="selector-label"
                        name="country"
                        size={isMobile ? "small" : "medium"}
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
                      size={isMobile ? "small" : "medium"}
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
                      size={isMobile ? "small" : "medium"}
                      value={formik.values.firstName ?? ""}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      error={
                        formik.touched.firstName &&
                        Boolean(formik.errors.firstName)
                      }
                      helperText={
                        formik.touched.firstName && formik.errors.firstName
                      }
                    />
                    <TextField
                      fullWidth
                      name="lastName"
                      label="Last Name"
                      size={isMobile ? "small" : "medium"}
                      value={formik.values.lastName ?? ""}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      error={
                        formik.touched.lastName &&
                        Boolean(formik.errors.lastName)
                      }
                      helperText={
                        formik.touched.lastName && formik.errors.lastName
                      }
                    />
                    <TextField
                      fullWidth
                      name="postalCode"
                      label="Postal Code"
                      size={isMobile ? "small" : "medium"}
                      value={formik.values.postalCode ?? ""}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      error={
                        formik.touched.postalCode &&
                        Boolean(formik.errors.postalCode)
                      }
                      helperText={
                        formik.touched.postalCode && formik.errors.postalCode
                      }
                    />
                    <TextField
                      fullWidth
                      name="street"
                      label="Street"
                      size={isMobile ? "small" : "medium"}
                      value={formik.values.street ?? ""}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      error={
                        formik.touched.street && Boolean(formik.errors.street)
                      }
                      helperText={formik.touched.street && formik.errors.street}
                    />
                    <TextField
                      fullWidth
                      name="phoneNumber"
                      label="Phone Number"
                      size={isMobile ? "small" : "medium"}
                      value={formik.values.phoneNumber ?? ""}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      error={
                        formik.touched.phoneNumber &&
                        Boolean(formik.errors.phoneNumber)
                      }
                      helperText={
                        formik.touched.phoneNumber && formik.errors.phoneNumber
                      }
                    />
                    <TextField
                      fullWidth
                      name="addressLine1"
                      label="Address Line 1"
                      size={isMobile ? "small" : "medium"}
                      value={formik.values.addressLine1 ?? ""}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      error={
                        formik.touched.addressLine1 &&
                        Boolean(formik.errors.addressLine1)
                      }
                      helperText={
                        formik.touched.addressLine1 &&
                        formik.errors.addressLine1
                      }
                    />
                    <TextField
                      fullWidth
                      name="addressLine2"
                      label="Address Line 2"
                      size={isMobile ? "small" : "medium"}
                      value={formik.values.addressLine2 ?? ""}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      error={
                        formik.touched.addressLine2 &&
                        Boolean(formik.errors.addressLine2)
                      }
                      helperText={
                        formik.touched.addressLine2 &&
                        formik.errors.addressLine2
                      }
                    />
                  </div>
                </div>
                <div ref={shippingMethodsRef}>
                  <h1 className="font-bold text-2xl">
                    2. Choose delivery method
                  </h1>
                  <div className="w-full flex flex-col gap-6 py-6 border-b-[1px] border-gray-100">
                    {shippingMethods?.map((method) => (
                      <div
                        key={method.id}
                        className={`w-full flex justify-between items-center p-4 outline hover:outline-4 hover:outline-secondary-color cursor-pointer max-md:flex-col ${
                          selectedShippingMethod?.id === method.id
                            ? "outline outline-4 outline-secondary-color"
                            : "outline outline-1 outline-gray-300"
                        }`}
                        onClick={() => {
                          setSelectedShippingMethod(method);
                          if (shippingMethodsRef.current) {
                            setTimeout(() => {
                              shippingMethodsRef?.current?.scrollIntoView({
                                behavior: "smooth",
                                block: "start",
                              });
                            }, 500);
                          }
                        }}
                      >
                        <div className="flex gap-6 items-center">
                          <img
                            src={`/${getShippingIcon(method.name)}`}
                            alt=""
                            className="w-16 h-16 object-contain"
                          />
                          <div className="flex flex-col font-semibold">
                            <span>{method.name}</span>
                            <span className="max-md:text-sm">
                              Estimated delivery time: 24 hours on working days
                            </span>
                          </div>
                        </div>
                        <span className="w-max text-lg font-bold max-md:items-start max-md:mt-[2px]">
                          {method.price} $
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
                {formik.isValid && selectedShippingMethod && (
                  <div ref={paymentRef}>
                    <h1 className="font-bold text-2xl">
                      3. Choose payment method
                    </h1>
                    <div className="w-full flex flex-col gap-6 py-6 border-b-[1px] border-gray-100">
                      {userPaymentMethods &&
                        userPaymentMethods.map((paymentMethod) => (
                          <div
                            key={paymentMethod.id}
                            className={`w-full flex gap-6 items-center p-4 outline hover:outline-4 hover:outline-secondary-color cursor-pointer max-md:flex-col max-md:gap-2 max-md:items-start ${
                              selectedPayment === paymentMethod.id
                                ? "outline outline-4 outline-secondary-color"
                                : "outline outline-1 outline-gray-300"
                            }`}
                            onClick={() => setSelectedPayment(paymentMethod.id)}
                          >
                            <img
                              src={`/${getPaymentIcon(paymentMethod.provider)}`}
                              alt=""
                              className="w-16 h-16 object-contain"
                            />
                            <span className="text-lg font-bold capitalize">
                              {paymentMethod?.provider}
                            </span>
                            <div className="flex flex-col">
                              <span>
                                Card number: **** **** **** ****{" "}
                                {paymentMethod?.last4CardNumber}
                              </span>
                              <span>
                                Expiry date:{" "}
                                {formatDateShort(paymentMethod?.expiryDate)}
                              </span>
                            </div>
                          </div>
                        ))}
                    </div>
                    <div className="flex flex-col gap-4">
                      {paymentType.map((payment) => (
                        <div
                          key={payment?.id}
                          className={`w-full flex gap-6 items-center p-4 outline hover:outline-4 hover:outline-secondary-color cursor-pointer ${
                            selectedPayment === payment?.id
                              ? "outline outline-4 outline-secondary-color"
                              : "outline outline-1 outline-gray-300"
                          } ${stripeImageLoaded ? "" : "hidden"}`}
                          onClick={() => setSelectedPayment(payment?.id)}
                        >
                          <img
                            src={`/${getPaymentIcon(payment?.value)}`}
                            alt=""
                            className="w-16 h-16 object-contain"
                            onLoad={() => setStripeImageLoaded(true)}
                          />
                          <span className="text-lg font-bold capitalize">
                            {payment?.value}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
            <div className="w-1/3 flex overflow-y-scroll max-lg:w-full">
              <div className="w-full flex flex-col gap-10 p-4 border-[1px] border-gray-100">
                <div className="w-full flex justify-between items-center">
                  <h1 className="font-bold text-2xl">Shopping list</h1>
                  <div
                    className="p-2 bg-gray-100 rounded-full cursor-pointer hover:bg-gray-700 hover:text-white"
                    onClick={() => navigate("/basket")}
                  >
                    <EditIcon />
                  </div>
                </div>
                {cart?.shoppingCartItems.map((item, index) => {
                  const selectedVariant =
                    item?.productItem?.productItemOneByColour?.find(
                      (variant) =>
                        variant.id === item?.productItem?.productItemId
                    );
                  const sizeVariation = selectedVariant?.variations?.find(
                    (v) => v.name === "size"
                  );
                  const availableQty = selectedVariant?.qtyInStock ?? 0;

                  const hasInsufficientStock = item.qty > availableQty;

                  const unitPrice = selectedVariant
                    ? selectedVariant.discount > 0
                      ? selectedVariant.price *
                        (1 - selectedVariant.discount / 100)
                      : selectedVariant.price
                    : 0;

                  return (
                    <div
                      key={index}
                      className="flex border-b-[1px] border-gray-100 py-2 gap-3"
                    >
                      <img
                        src={item?.productItem?.productImages[0]?.imageFilename}
                        alt=""
                        className="w-36 h-36 object-contain"
                      />
                      <div className="w-full flex flex-col justify-between">
                        <div className="flex flex-col">
                          <span className="font-bold">
                            {item?.productItem?.productName}
                          </span>
                          <span className="text-gray-500">
                            {
                              item?.productItem?.productItemOneByColour[0]
                                ?.productCode
                            }
                          </span>
                          {hasInsufficientStock && (
                            <span className="text-red-500 font-semibold text-sm">
                              Only {availableQty} item(s) left in stock!
                            </span>
                          )}
                        </div>
                        <div className="flex justify-between items-end">
                          <div className="flex flex-col text-sm">
                            {sizeVariation?.options?.[0]?.value && (
                              <span>
                                Size:{" "}
                                <b>
                                  {sizeVariation.options[0].value.toUpperCase()}
                                </b>
                              </span>
                            )}
                            <span className="text-sm">
                              Quantity {item.qty} for{" "}
                              {(unitPrice * item.qty).toFixed(2)} $
                            </span>
                          </div>
                          <span className="font-bold">
                            {(unitPrice * item.qty).toFixed(2)} $
                          </span>
                        </div>
                      </div>
                    </div>
                  );
                })}
                <div className="flex flex-col gap-4">
                  <h1 className="font-bold text-3xl">Summary</h1>
                  <div className="flex flex-col gap-4">
                    <div className="flex justify-between items-center text-lg py-4 border-b-[1px] border-gray-100">
                      <span>Products (total):</span>
                      <span>{totalPrice()} $</span>
                    </div>
                    {cart?.discountCodes && cart?.discountCodes.length > 0 && (
                      <div className="flex flex-col gap-2 py-4 border-b-[1px] border-gray-100">
                        {cart.discountCodes.map((code) => (
                          <div
                            key={code.id}
                            className="flex justify-between text-lg"
                          >
                            <span>Discount: </span>
                            <span className="text-red-300">
                              - {code.discount} %
                            </span>
                          </div>
                        ))}
                      </div>
                    )}
                    {selectedShippingMethod && (
                      <div className="flex justify-between items-center text-lg py-4 border-b-[1px] border-gray-100">
                        <span>Delivery:</span>
                        <span>{selectedShippingMethod?.price} $</span>
                      </div>
                    )}
                    <div className="flex justify-between items-center text-lg font-bold py-4 border-b-[1px] border-gray-100">
                      <span>To Payment</span>
                      <span>
                        {(
                          parseFloat(calculateTotalCartValue()) +
                          (selectedShippingMethod?.price ?? 0)
                        ).toFixed(2)}{" "}
                        $
                      </span>
                    </div>
                  </div>
                </div>
                <button
                  className="h-[55px] w-full flex bg-black text-white rounded-3xl text-center items-center justify-center font-bold cursor-pointer disabled:bg-gray-300 disabled:cursor-default"
                  disabled={
                    !(
                      formik.isValid &&
                      selectedShippingMethod &&
                      selectedPayment
                    ) ||
                    hasUnavailableProducts ||
                    isSubmitting
                  }
                  onClick={() => submitOrder()}
                >
                  {isSubmitting ? (
                    <div className="flex items-center justify-center h-[100%]">
                      <LoadingAnimation height={50} width={50} />
                    </div>
                  ) : (
                    "Order and pay"
                  )}
                </button>
              </div>
            </div>
          </>
        ) : (
          <div className="text-3xl font-bold text-center mt-12">
            Your basket is empty
          </div>
        )}
      </div>
    </>
  );
};

export default Delivery;
