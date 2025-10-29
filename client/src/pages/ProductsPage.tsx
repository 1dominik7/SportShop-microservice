import { useEffect, useState } from "react";
import NavProducts from "../components/productsPage/NavProducts";
import { useLocation, useNavigate, useSearchParams } from "react-router";
import {
  useProductItemsFilters,
  useProductsByFiltersGrouped,
} from "../hooks/query";
import LeftProducts from "../components/productsPage/LeftProducts";
import RightProducts from "../components/productsPage/RightProducts";
import { Filter, Variation, VariationOption } from "../types/userTypes";
import LoadingAnimation from "../ui/LoadingAnimation";

type SelectedOption = {
  [key: number]: { optionId: number[]; filteredName: string[] };
};

const ProductsPage = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [selectedOption, setSelectedOption] = useState<SelectedOption>({});
  const [pageSize, setPageSize] = useState<string>(
    localStorage.getItem("pageSize") || "24"
  );
  const [sortBy, setSortBy] = useState<string>(
    localStorage.getItem("sortBy") || "productId"
  );
  const [sortOrder, setSortOrder] = useState<string>(
    localStorage.getItem("sortOrder") || "desc"
  );
  const [pageNumber, setPageNumber] = useState(
    searchParams.get("page") ? parseInt(searchParams.get("page")!) - 1 : 0
  );

  const [formattedFilters, setFormattedFilters] = useState<Filter[]>([]);

  const pathname = useLocation().pathname;
  const categoryId = searchParams.get("category")
    ? parseInt(searchParams.get("category")!)
    : 1;

  const { data, isFetching } = useProductsByFiltersGrouped(
    categoryId,
    selectedOption,
    pageNumber,
    Number(pageSize),
    sortBy,
    sortOrder
  );

  const { data: filters } = useProductItemsFilters(categoryId, selectedOption);

  useEffect(() => {
    if (pageNumber > 0) {
      const params = new URLSearchParams();
      searchParams.forEach((value, key) => {
        params.set(key, value);
      });
      params.set("page", (pageNumber + 1).toString());
      navigate(`${pathname}?${params.toString()}`);
    } else {
      const params = new URLSearchParams();
      searchParams.forEach((value, key) => {
        if (key !== "page") {
          params.set(key, value);
        }
      });
      navigate(`${pathname}?${params.toString()}`, {
        replace: true,
      });
    }
  }, [pageNumber, searchParams, navigate, pathname]);

  useEffect(() => {
    if (data) {
      const variationsMap: { [key: string]: Variation } = {};

      data?.content?.forEach((product: any) => {
        product.variations.forEach((variation: any) => {
          if (variationsMap[variation.name]) {
            const existingOptions = variationsMap[variation.name].options.map(
              (opt) => opt.id
            );
            variation.options.forEach((option: VariationOption) => {
              if (!existingOptions.includes(option.id)) {
                variationsMap[variation.name].options.push(option);
              }
            });
          } else {
            variationsMap[variation.name] = {
              id: variation.id,
              categoryId: variation.categoryId,
              name: variation.name,
              options: variation.options,
            };
          }
        });
      });

      const sortedVariations = Object.values(variationsMap).sort(
        (a, b) => a.id - b.id
      );

      sortedVariations.forEach((variation) => {
        variation.options.sort((a, b) => a.id - b.id);
      });

      setFormattedFilters(sortedVariations);
    }
  }, [data]);

  useEffect(() => {
    const filtersValue = searchParams.get("filters");


     if (!filtersValue) {
    setSelectedOption({});
    return;
  }
    const initialSelectedOption: SelectedOption = {};
    const filterMatches = filtersValue.match(/(\d+)\[(\d+(?:%\d+)*)\]/g);

    if (filterMatches) {
      filterMatches.forEach((match) => {
        const matchGroups = match.match(/(\d+)\[([\d%]+)\]/);
        if (matchGroups) {
          const variationId = parseInt(matchGroups[1]);
          const optionIds = matchGroups[2].split("%").map(Number);

          initialSelectedOption[variationId] = {
            optionId: optionIds,
            filteredName: optionIds.map(() => ""),
          };
        }
      });
    }

    setSelectedOption(initialSelectedOption);
    setPageNumber(0);
  }, [searchParams.get("filters"), categoryId]);

  return (
    <div className="relative flex flex-col mt-[120px] min-h-full max-lg:mt-[100px]">
            {isFetching && (
        <div className="absolute top-0 left-0 flex w-full h-full justify-center items-center bg-white opacity-60 z-50">
          <LoadingAnimation />
        </div>
      )}
      <>
        <div className="flex flex-col gap-6 pb-6 border-b-[1px] border-gray-100 max-lg:pb-2">
          <NavProducts numberOfProducts={data?.totalElements ?? 0} />
        </div>
        <div className="flex max-md:flex-col">
          <LeftProducts
            selectedOption={selectedOption}
            setSelectedOption={setSelectedOption}
            filters={filters}
            isFetching={isFetching}
            setPageNumber={setPageNumber}
            pageNumber={pageNumber}
          />
          {data && (
            <RightProducts
              pageSize={pageSize}
              setPageSize={setPageSize}
              pageNumber={pageNumber}
              setPageNumber={setPageNumber}
              sortBy={sortBy}
              setSortBy={setSortBy}
              sortOrder={sortOrder}
              setSortOrder={setSortOrder}
              selectedOption={selectedOption}
              setSelectedOption={setSelectedOption}
              data={data}
              filters={filters}
            />
          )}
        </div>
      </>
    </div>
  );
};

export default ProductsPage;
