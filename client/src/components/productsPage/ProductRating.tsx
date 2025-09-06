import { Rating } from "@mui/material";
import { ReviewRate } from "../../types/userTypes";

const ProductRating = ({
  productId,
  reviews,
}: {
  productId: number;
  reviews: ReviewRate[];
}) => {
  const review = reviews?.find((r) => r?.productId === productId);
  if (!review) return null;

  return (
    <div className="flex items-center gap-2 mt-1">
      <Rating
        name="read-only"
        value={review?.averageRating}
        precision={0.5}
        readOnly
        size="small"
        sx={{
          '& .MuiRating-icon': {
            fontSize:{
              xs:"12px",
              sm:"20px"
            }
          }}}
      />
      <span className="text-xs text-gray-600">({review?.totalReviews})</span>
    </div>
  );
};

export default ProductRating;
