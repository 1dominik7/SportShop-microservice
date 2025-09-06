import React, {  useEffect, useState } from "react";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import RadioButtonUncheckedIcon from "@mui/icons-material/RadioButtonUnchecked";
import RadioButtonCheckedIcon from "@mui/icons-material/RadioButtonChecked";
import { useLocation, useNavigate } from "react-router";
import { ProductItemsFilters, VariationOption } from "../../types/userTypes";

type SelectedOption = {
  [key: number]: { optionId: number[]; filteredName: string[] };
};

interface Props {
  selectedOption: SelectedOption;
  setSelectedOption: React.Dispatch<React.SetStateAction<SelectedOption>>;
  filters: ProductItemsFilters[] | undefined;
  isFetching: boolean;
  setPageNumber: React.Dispatch<React.SetStateAction<number>>;
  pageNumber: number;
}

const LeftProducts = ({
  selectedOption,
  setSelectedOption,
  filters,
  isFetching,
  setPageNumber,
  pageNumber,
}: Props) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [openVariation, setOpenVariation] = useState<Record<number, boolean>>(
    {}
  );
  const { pathname } = useLocation();

  const customizSize = ["sx", "s", "m", "l", "xl", "xxl"];

  const sortOptions = (options: VariationOption[]) => {
    return options?.sort((a, b) => {
      const aVal = a.value;
      const bVal = b.value;

      if (typeof aVal === "string" && typeof bVal === "string") {
        if (customizSize.includes(aVal) && customizSize.includes(bVal)) {
          return customizSize.indexOf(aVal) - customizSize.indexOf(bVal);
        }

        const aNumber = parseFloat(aVal);
        const bNumber = parseFloat(bVal);

        if (!isNaN(aNumber) && !isNaN(bNumber)) {
          return aNumber - bNumber;
        }

        return aVal.localeCompare(bVal);
      }

      if (typeof aVal === "number" && typeof bVal === "number") {
        return aVal - bVal;
      }

      return 0;
    });
  };

  const handleOpenToggle = (id: number) => {
    setOpenVariation((prev) => ({
      ...prev,
      [id]: !prev[id],
    }));
  };

  const handleSelectOption = (
    variationId: number,
    optionId: number,
    filteredName: string
  ) => {
    setPageNumber(0);
    setSelectedOption((prev) => {
      const newSelection = { ...prev };
      const currentOptionIds = newSelection[variationId]?.optionId || [];
      if (currentOptionIds.includes(optionId)) {
        newSelection[variationId].optionId = currentOptionIds.filter(
          (id) => id !== optionId
        );
        newSelection[variationId].filteredName = newSelection[
          variationId
        ].filteredName.filter((name) => name !== filteredName);

        if (newSelection[variationId].optionId.length === 0) {
          delete newSelection[variationId];
        }
      } else {
        if (!newSelection[variationId]) {
          newSelection[variationId] = { optionId: [], filteredName: [] };
        }
        newSelection[variationId].optionId.push(optionId);
        newSelection[variationId].filteredName.push(filteredName);
      }

      return newSelection;
    });
  };

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);

    const filters: string[] = Object.entries(selectedOption).map(
      ([variationIdStr, { optionId }]) => {
        const variationId = parseInt(variationIdStr);
        const optionIdsStr = optionId.join("%");
        return `${variationId}[${optionIdsStr}]`;
      }
    );
    if (filters.length > 0) {
      searchParams.set("filters", filters.join("~"));
    } else {
      searchParams.delete("filters");
    }

    const newUrl = `${pathname}?${searchParams.toString()}`;
    navigate(newUrl, { replace: true });
  }, [selectedOption, navigate, filters]);

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    const filtersParam = searchParams.get("filters");

    if (filtersParam && filters) {
      const filterEntries = filtersParam.split("~").map((filter) => {
        const [variationIdStr, optionIdsStr] = filter.split("[");
        const variationId = parseInt(variationIdStr);
        const optionIds = optionIdsStr
          .replace("]", "")
          .split("%")
          .map((id) => parseInt(id));

        return { variationId, optionIds };
      });

      const newSelectedOption: SelectedOption = {};
      filterEntries.forEach(({ variationId, optionIds }) => {
        const variation = filters.find(
          (item) => item.variation.id === variationId
        )?.variation;

        const selectedOptionNames = variation
          ? variation.options
              .filter((option) => optionIds.includes(option.id))
              .map((option) => option.value)
          : [];

        newSelectedOption[variationId] = {
          optionId: optionIds,
          filteredName: selectedOptionNames,
        };
      });

      setSelectedOption(newSelectedOption);
    } else {
      setSelectedOption({});
    }
  }, [navigate]);

  return (
    <div className="flex w-[400px] border-r-2 border-gray-100 max-md:w-full">
      <div className="w-full mt-6">
        {filters &&
          filters?.map((item) => {
            const variation = item?.variation;

            return (
              <div key={variation?.id}>
                <div
                  onClick={() => handleOpenToggle(variation.id)}
                  className="w-full flex items-center justify-between p-4 px-6 bg-gray-100 cursor-pointer border-b-[1px] border-b-gray-200"
                >
                  <span className="font-semibold text-xl capitalize max-lg:text-base">
                    {variation?.name}
                  </span>
                  <KeyboardArrowDownIcon
                    className={`transform transition-transform duration-300 ease-in-out ${
                      openVariation[variation?.id]
                        ? "animate-rotate"
                        : "animate-rotate-reverse"
                    }`}
                  />
                </div>
                <div
                  className={`overflow-y-scroll px-4 my-[16px] transition-height duration-300 ease-in-out${
                    openVariation[variation?.id]
                      ? "flex flex-col space-y-2 max-h-[200px]"
                      : "max-h-0 my-[2px]"
                  }`}
                >
                  {variation?.options &&
                    sortOptions(variation.options).map((option) => (
                      <div
                        key={option.id}
                        onClick={() => {
                          handleSelectOption(
                            variation.id,
                            option.id,
                            variation.name
                          );
                        }}
                      >
                        {openVariation[variation?.id] && (
                          <div className="flex items-center gap-2 cursor-pointer">
                            {selectedOption[variation?.id]?.optionId?.includes(
                              option.id
                            ) ? (
                              <RadioButtonCheckedIcon
                                style={{ color: "#6BE140" }}
                              />
                            ) : (
                              <RadioButtonUncheckedIcon
                                style={{ color: "gray" }}
                              />
                            )}
                            <span className="text-base capitalize max-lg:text-sm">
                              {option.value}
                            </span>
                          </div>
                        )}
                      </div>
                    ))}
                </div>
              </div>
            );
          })}
      </div>
    </div>
  );
};

export default LeftProducts;
