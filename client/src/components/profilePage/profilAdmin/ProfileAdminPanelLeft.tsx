import { useLocation, useNavigate } from 'react-router';

const ProfileAdminPanelLeft = () => {

  const navigate = useNavigate();
  const location = useLocation();
  const selectedOption = location.pathname.split("/").pop();

  return (
    <div className='h-full px-6 py-6 lg:px-20 max-lg:px-4 max-lg:py-2'>
      <div className='flex w-[220px] flex-col gap-4 font-bold text-lg text-center max-lg:w-full max-lg:gap-2'>
        <div className={`py-2 px-6 hover:text-gray-500 cursor-pointer ${selectedOption === 'products' && 'bg-gray-100 rounded-full'}`} onClick={() => navigate('products')}>Products</div>
        <div className={`py-2 px-6 hover:text-gray-500 cursor-pointer ${selectedOption === 'addProduct' && 'bg-gray-100 rounded-full'}`} onClick={() => navigate('addProduct')}>Add Product</div>
        <div className={`py-2 px-6 hover:text-gray-500 cursor-pointer ${selectedOption === 'categories' && 'bg-gray-100 rounded-full'}`} onClick={() => navigate('categories')}>Categories</div>
        <div className={`py-2 px-6 hover:text-gray-500 cursor-pointer ${selectedOption === 'variation' && 'bg-gray-100 rounded-full'}`} onClick={() => navigate('variation')}>Variations</div>
        <div className={`py-2 px-6 hover:text-gray-500 cursor-pointer ${selectedOption === 'variationOption' && 'bg-gray-100 rounded-full'}`} onClick={() => navigate('variationOption')}>Variation Options</div>
        <div className={`py-2 px-6 hover:text-gray-500 cursor-pointer ${selectedOption === 'discount' && 'bg-gray-100 rounded-full'}`} onClick={() => navigate('discount')}>Discount</div>
      </div>
    </div>
  )
}

export default ProfileAdminPanelLeft
 