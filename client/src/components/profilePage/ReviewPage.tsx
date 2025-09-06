import { useEffect, useState } from "react";
import LoadingAnimation from "../../ui/LoadingAnimation";
import { useNavigate, useParams } from "react-router";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import {
  FormControl,
  InputLabel,
  MenuItem,
  Rating,
  Select,
  TextField,
} from "@mui/material";
import { api } from "../../config/api";
import { GetShopOrder, UserReview } from "../../types/userTypes";

const ReviewPage = () => {
  const navigate = useNavigate();
  const { orderId } = useParams();

  const [order, setOrder] = useState<GetShopOrder | null>(null);
  const [canReview, setCanReview] = useState<Map<number, boolean>>(new Map());
  const [rating, setRating] = useState<number | null>(null);
  const [comment, setComment] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [selectedProductId, setSelectedProductId] = useState<number | null>(
    null
  );
  const [selectedOrderLineId, setSelectedOrderLineId] = useState<number | null>(
    null
  );
  const [refreshTrigger, setRefreshTrigger] = useState<number>(0);
  const [reviews, setReviews] = useState<UserReview[] | null>(null);
  const [editingReviewId, setEditingReviewId] = useState<number | null>(null);
  const [isEditing, setIsEditing] = useState<boolean>(false);
  const [isDelete, setIsDelete] = useState<number | null>(null);

  useEffect(() => {
    const getShopOrderById = async () => {
      try {
        const res = await api.get(`/api/v1/shop-order/user/${orderId}`);
        setOrder(res.data);
      } catch (error) {
        console.error(error);
      }
    };
    getShopOrderById();
  }, [orderId]);

  useEffect(() => {
    const getReviewByOrderLineId = async () => {
      if (!order) return;
      try {
        const OrderLineIds = order?.orderLines.map((item) => item.id);
        
        const res = await api.get(
          `/api/v1/review/byOrderLines?orderLineIds=${OrderLineIds.join(",")}`
        );
        setReviews(res.data);
      } catch (error) {
        console.error(error);
      }
    };
    getReviewByOrderLineId();
  }, [order, refreshTrigger]);

  const handleEditReview = (review: UserReview) => {
    const orderLine = order?.orderLines.find(
      (line) => line.id === review.orderLineId
    );

    if (orderLine?.productItem?.id) {
      setSelectedProductId(orderLine.productItem.id);
      setSelectedOrderLineId(review.orderLineId);
      setRating(review.ratingValue);
      setComment(review.comment);
      setEditingReviewId(review.id!);
      setIsEditing(true);
    }
  };

  const handleSubmit = async () => {
    const userReviewRequest = {
      productId: selectedProductId,
      orderLineId: selectedOrderLineId,
      ratingValue: rating,
      comment: comment,
    };

    if (rating && comment.trim()) {
      try {
        setIsLoading(true);
        if (isEditing && editingReviewId) {
          await api.put(
            `/api/v1/review/${editingReviewId}`,
            userReviewRequest
          );
        } else {
          await api.post(`/api/v1/review`, userReviewRequest);
        }
        setRating(0);
        setComment("");
        setSelectedOrderLineId(null);
        setEditingReviewId(null);
        setIsEditing(false);
        setSelectedProductId(null);
      } catch (error) {
        console.log(error);
      } finally {
        setIsLoading(false);
        setRefreshTrigger((prev) => prev + 1);
      }
    }
  };

  useEffect(() => {
    const checkReviewEligibility = async () => {
      if (!order) return;

      try {
        const OrderLineIds = order.orderLines.map((item) => item.id);
        const res = await api.get(
          `/api/v1/review/products/can-review?orderLineIds=${OrderLineIds.join(
            ","
          )}`
        );

        const data = res.data;
        const eligibilityMap = new Map<number, boolean>(
          Object.entries(data).map(([key, value]) => [
            Number(key),
            value as boolean,
          ])
        );

        setCanReview(eligibilityMap);
      } catch (error) {
        console.error("Error checking review eligibility:", error);
      }
    };

    checkReviewEligibility();
  }, [order, refreshTrigger]);

  const confirmDeleteHandler = (reviewId: number) => {
    setIsDelete(reviewId);
  };

  const deleteHandler = async (reviewId: number) => {
    try {
      setIsLoading(true);

      await api.delete(`/api/v1/review/${reviewId}`);
    } catch (error) {
      console.log(error);
    } finally {
      setIsLoading(false);
      setRefreshTrigger((prev) => prev + 1);
    }
  };

  return (
    <div className="py-11 px-20 max-md:px-6 max-sm:py-4">
      {isLoading ? (
        <div className="fixed top-0 left-0 flex h-full w-full z-50 bg-white opacity-80 items-center justify-center">
          <LoadingAnimation />
        </div>
      ) : (
        <div className="flex flex-col gap-6 max-sm:gap-2">
          <div className="flex items-center">
            <span
              className="cursor-pointer hover:underline"
              onClick={() => navigate("/profile/orders")}
            >
              Orders
            </span>
            <ChevronRightIcon />
            <span className="text-gray-400">Reviews</span>
          </div>
          <div className="flex flex-col gap-4 border-b-[1px] border-gray-300 pb-4">
            <div className="flex justify-between items-center gap-2 font-bold">
              <span>Order number: {order?.id}</span>
            </div>
            <div className="flex flex-wrap gap-6 max-sm:flex-col max-sm:gap-2">
              {order?.orderLines?.map((item) => (
                <div
                  key={`${item.id}-${item?.productItem?.id}`}
                  className="flex flex-col gap-6 py-2"
                >
                  <div
                    className="w-[200px] flex flex-col cursor-pointer max-sm:flex-row gap-2 max-sm:text-sm"
                    onClick={() => {
                      const colour = item?.productItem?.variationOptions?.find(
                        (v) => v?.variation?.name.toLowerCase() === "colour"
                      )?.value;

                      navigate(
                        `/products/${item?.productItem?.productId}-${colour}`
                      );
                    }}
                  >
                    <img
                      className="h-[300px] object-cover max-sm:h-[100px] max-sm:w-full max-sm:object-contain"
                      src={item?.productItem?.productImages[0]?.imageFilename}
                      alt=""
                    />
                    <span className="w-full">
                      <b>{item?.id}.</b> {item?.productName}
                    </span>
                  </div>
                </div>
              ))}
              <div className="min-w-[300px] flex flex-col gap-2 max-sm:w-full max-sm:text-sm">
                {order?.orderLines?.filter(
                  (item) =>
                    typeof item.id === "number" && canReview.get(item?.id)
                )?.length !== 0 && (
                  <FormControl fullWidth>
                    <InputLabel>Select product to review</InputLabel>
                    <Select
                      value={selectedOrderLineId || ""}
                      onChange={(e) => {
                        const orderLineId = Number(e.target.value);
                        const selectedOrderLine = order?.orderLines.find(
                          (item) => item.id === orderLineId
                        );
                        setSelectedOrderLineId(orderLineId);
                        setSelectedProductId(
                          selectedOrderLine?.productItem.id || null
                        );
                      }}
                      label="Select product to review"
                      sx={{ fontSize: '0.875rem' }}
                    >
                      {order?.orderLines
                        .filter((item) => canReview.get(item.id!))
                        .map((item) => (
                          <MenuItem key={item.id} value={item.id} sx={{ fontSize: '0.875rem' }}>
                            {item?.id}. {item.productName}
                          </MenuItem>
                        ))}
                    </Select>
                  </FormControl>
                )}
                {selectedProductId && !isEditing && (
                  <div className="flex flex-col gap-4 mt-4">
                    <Rating
                      name="product-rating"
                      value={rating}
                      onChange={(_, newValue) => setRating(newValue)}
                    />
                    <TextField
                      multiline
                      fullWidth
                      rows={4}
                      placeholder="Write your review here..."
                      value={comment}
                      onChange={(e) => setComment(e.target.value)}
                      variant="outlined"
                    />

                    <button
                      className={`p-4 bg-black text-white font-bold disabled:bg-gray-400`}
                      onClick={handleSubmit}
                      disabled={!rating || !comment.trim()}
                    >
                      Add Review
                    </button>
                  </div>
                )}
                {reviews !== null && (
                  <div className="flex flex-wrap gap-2">
                    {reviews?.map((review) => (
                      <div key={review.id} className="w-full">
                        {editingReviewId === review.id ? (
                          <div
                            className="flex flex-col gap-4 mt-4"
                            key={review.id}
                          >
                            <Rating
                              name="product-rating"
                              value={rating}
                              onChange={(_, newValue) => setRating(newValue)}
                            />
                            <TextField
                              multiline
                              fullWidth
                              rows={4}
                              placeholder="Write your review here..."
                              value={comment}
                              onChange={(e) => setComment(e.target.value)}
                              variant="outlined"
                            />
                            <div className="flex gap-2">
                              <button
                                className={`p-4 bg-black text-white font-bold disabled:bg-gray-400 flex-1`}
                                onClick={handleSubmit}
                              >
                                Update Review
                              </button>
                              <button
                                className="p-4 border border-gray-300 text-gray-700 font-bold flex-1"
                                onClick={() => setEditingReviewId(null)}
                              >
                                Cancel
                              </button>
                            </div>
                          </div>
                        ) : (
                          <div
                            key={review.id}
                            className="relative w-[250px] flex flex-col gap-2 p-4 border rounded bg-gray-50 shadow-sm max-sm:w-full"
                          >
                            <span>Product item no.{review?.orderLineId}</span>
                            <div className="text-sm text-gray-600 mt-1">
                              {review?.comment || "No comment"}
                            </div>
                            <div className="flex items-center gap-2">
                              <span>Rating:</span>
                              <Rating value={review?.ratingValue} readOnly />
                            </div>
                            <div className="flex flex-col">
                              <span>{review?.userName}</span>
                              <span>
                                {new Date(
                                  review?.createdDate
                                ).toLocaleDateString("en-GB")}
                              </span>
                            </div>
                            <button
                              className="bg-black font-bold p-2 text-white cursor-pointer hover:opacity-80"
                              onClick={() => handleEditReview(review)}
                            >
                              Edit
                            </button>
                            <button
                              className="bg-red-500 font-bold p-2 text-white cursor-pointer hover:opacity-80"
                              onClick={() => confirmDeleteHandler(review.id!)}
                            >
                              Delete
                            </button>
                            {isDelete === review.id && (
                              <div className="absolute w-full h-full top-0 left-0 bg-white flex flex-col items-center justify-center gap-4 text-center opacity-95">
                                <span className="font-bold">
                                  Are you sure to delete this review ?
                                </span>
                                <div className="flex gap-4">
                                  <button
                                    className="p-2 bg-red-500 font-bold text-white cursor-pointer hover:opacity-80"
                                    onClick={() => deleteHandler(review.id!)}
                                  >
                                    Delete
                                  </button>
                                  <button
                                    onClick={() => setIsDelete(null)}
                                    className="border-2 border-black p-2 font-bold cursor-pointer"
                                  >
                                    Cancel
                                  </button>
                                </div>
                              </div>
                            )}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ReviewPage;
