import { useEffect, useMemo, useState } from "react";
import GroupIcon from "@mui/icons-material/Group";
import InventoryIcon from "@mui/icons-material/Inventory";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import AttachMoneyIcon from "@mui/icons-material/AttachMoney";
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import TrendingDownIcon from "@mui/icons-material/TrendingDown";
import TrendingUpIcon from "@mui/icons-material/TrendingUp";
import TrendingFlatIcon from "@mui/icons-material/TrendingFlat";
import ReportProblemIcon from "@mui/icons-material/ReportProblem";
import { OrderStatusStatistics } from "../../../types/userTypes";
import { api } from "../../../config/api";
import { format, subMonths } from "date-fns";
import { useNavigate } from "react-router";
import {
  useLatestSalesProducts,
  useShopOrderStatistics,
  useStatistics,
  useTopProductSales,
} from "../../../hooks/query";
import LoadingAnimation from "../../../ui/LoadingAnimation";

const ProfileAdminStatistics = () => {
  const COLORS = ["#8884d8", "#82ca9d", "#2133dd", "#dbdd73", "#eb7e7e"];

  const navigate = useNavigate();
  const now = new Date();

  const last6Months = Array.from({ length: 6 }).map((_, index) =>
    subMonths(now, index)
  );

  const [orderStatuses, setOrderStatuses] = useState<
    OrderStatusStatistics[] | null
  >(null);
  const [selectedSalesRatioMonth, setSelectedSalesRatioMonth] =
    useState<string>(format(now, "yyyy-MM"));
  const [selectedTopProductsSalesMonth, setSelectedTopProductsSalesMonth] =
    useState<string>(format(now, "yyyy-MM"));
  const [limitProducts, setLimitProducts] = useState<number>(5);
  const [limitTopSalesProducts, setLimitTopSaleProducts] = useState<number>(3);

  function parseYearMonth(yearMonthStr: string) {
    const [yearStr, monthStr] = yearMonthStr.split("-");
    return { year: Number(yearStr), month: Number(monthStr) };
  }

  const { year, month } = parseYearMonth(selectedSalesRatioMonth);
  const { year: topYear, month: topMonth } = parseYearMonth(
    selectedTopProductsSalesMonth
  );

  const {
    data: statistics,
    isLoading: isStatisticsLoading,
    isError: isErrorStatistics,
  } = useStatistics();
  const {
    data: topProductSales,
    isLoading: isTopProductSalesLoading,
    isError: isErrorTopProductSales,
  } = useTopProductSales(topMonth, topYear, limitTopSalesProducts);
  const {
    data: latestProductsSales,
    isLoading: isLatestProductsSalesLoading,
    isError: isErrorLatestProductsSales,
  } = useLatestSalesProducts(limitProducts);
  const {
    data: shopOrderStatistics,
    isLoading: isShopOrderStatisticsLoading,
    isError: isErrorShopOrderStatistics,
  } = useShopOrderStatistics(month, year);

  useEffect(() => {
    const getOrderStatus = async () => {
      try {
        const res = await api.get("/api/v1/statistics/orderStatus");
        setOrderStatuses(res.data);
      } catch (error) {
        console.error(error);
      }
    };
    getOrderStatus();
  }, []);

  const orderStatusData = useMemo(() => {
    return (
      orderStatuses?.map((item) => ({
        name: item.statusName,
        value: item.count,
      })) || []
    );
  }, [orderStatuses]);

  const userData = [
    {
      name: format(subMonths(now, 2), "LLLL"),
      uv: statistics?.usersStatistics.usersTwoMonthsAgo,
    },
    {
      name: format(subMonths(now, 1), "LLLL"),
      uv: statistics?.usersStatistics.usersInLastMonth,
    },
    {
      name: format(now, "LLLL"),
      uv: statistics?.usersStatistics.usersInCurrentMonth,
    },
  ];

  const salesRatioData = shopOrderStatistics?.thisMonthWeeklySales.map(
    (cms, index) => ({
      name: `Week ${index + 1}`,
      cms,
      lms: shopOrderStatistics.lastMonthWeeklySales[index] ?? 0,
    })
  );

  const percentOfUsersThisMonth = useMemo(() => {
    const allUsers = statistics?.usersStatistics?.totalUsers;
    const thisMonth = statistics?.usersStatistics?.usersInCurrentMonth;

    if (!allUsers || thisMonth === undefined) return null;

    const percent = (thisMonth / allUsers) * 100;
    return percent.toFixed(2);
  }, [statistics]);

  const totalValue = useMemo(() => {
    return orderStatusData.reduce((acc, item) => acc + item.value, 0);
  }, [orderStatusData]);

  const salesRatioThisMonth =
    shopOrderStatistics?.thisMonthWeeklySales.reduce(
      (acc, item) => acc + item,
      0
    ) ?? 0;
  const salesRatioLastMonth =
    shopOrderStatistics?.lastMonthWeeklySales.reduce(
      (acc, item) => acc + item,
      0
    ) ?? 0;

  const calculateSalesRatioChange = () => {
    let salesRatioChangePercent: number;
    if (salesRatioLastMonth && salesRatioLastMonth !== 0) {
      salesRatioChangePercent =
        ((salesRatioThisMonth - salesRatioLastMonth) / salesRatioLastMonth) *
        100;
    } else if (salesRatioThisMonth > 0) {
      salesRatioChangePercent = 100;
    } else {
      salesRatioChangePercent = 0;
    }
    return salesRatioChangePercent;
  };

  const percentOfSalesRatio = calculateSalesRatioChange();

  const latestSalesProducts =
    latestProductsSales?.reduce((acc, item) => acc + item.price, 0) ?? 0;

  const topProductSalesData = topProductSales?.map((product) => {
    const sizeOption = product.productItem.variationOptions?.find(
      (v) => v?.variation?.name === "size"
    );

    const colourOption = product.productItem.variationOptions?.find(
      (v) => v?.variation?.name === "colour"
    );
    return {
      name: `${product.productItem.productName}${
        sizeOption ? " - " + sizeOption.value.toUpperCase() : ""
      }${colourOption ? " - " + colourOption.value : ""}`,
      totalQuantity: product.totalQuantity,
      productId: product.productItem.productId,
      colour: colourOption ? colourOption.value : "",
    };
  });

  const topProductsSalesQuantitySum =
    topProductSalesData?.reduce((acc, item) => acc + item?.totalQuantity, 0) ??
    0;

  return (
    <div className="bg-gray-200 w-[full]">
      <div className="w-full flex flex-col gap-4 p-4">
        {isErrorStatistics ? (
          <div className="flex flex-col items-center bg-white p-4 rounded-lg">
            <ReportProblemIcon fontSize="large" color="error" />
            <span className="font-bold text-xl">
              Cannot load statistics data
            </span>
          </div>
        ) : isStatisticsLoading ? (
          <div className="flex items-center justify-center w-full h-full z-50 bg-white opacity-80">
            <LoadingAnimation />
          </div>
        ) : (
          <div className="w-full flex justify-between pt-4 max-md:flex-col max-md:gap-2">
            <div className="flex justify-between w-[24%] border-b-4 border-blue-300 bg-white rounded-lg p-4 py-6 max-lg:py-2 max-md:w-full">
              <div className="flex flex-col gap-2 items-start">
                <GroupIcon fontSize="large" />
                <span className="max-md:text-sm">Users</span>
              </div>
              <span className="text-3xl max-xl:text-xl">
                {statistics?.usersStatistics?.totalUsers}
              </span>
            </div>
            <div className="flex justify-between w-[24%] border-b-4 border-blue-300 bg-white rounded-lg p-4 py-6 max-lg:py-2 max-md:w-full">
              <div className="flex flex-col gap-2 items-start">
                <InventoryIcon fontSize="large" />
                <span className="max-md:text-sm">Total Products</span>
              </div>
              <span className="text-3xl max-xl:text-xl">
                {statistics?.totalProducts}
              </span>
            </div>
            <div className="flex justify-between w-[24%] border-b-4 border-blue-300 bg-white rounded-lg p-4 py-6 max-lg:py-2 max-md:w-full">
              <div className="flex flex-col gap-2 items-start">
                <ShoppingCartIcon fontSize="large" />
                <span className="max-md:text-sm">Orders</span>
              </div>
              <span className="text-3xl max-xl:text-xl">
                {statistics?.totalOrders}
              </span>
            </div>
            <div className="flex justify-between w-[24%] border-b-4 border-blue-300 bg-white rounded-lg p-4 py-6 max-lg:py-2 max-md:w-full">
              <div className="flex flex-col gap-2 items-start">
                <AttachMoneyIcon fontSize="large" />
                <span className="max-md:text-sm">Total Incomes</span>
              </div>
              <span className="text-3xl max-xl:text-xl">
                {statistics?.totalIncomes}$
              </span>
            </div>
          </div>
        )}
        <div className="flex justify-between h-[500px] max-xl:h-full max-xl:flex-col max-xl:gap-4">
          <div className="h-full w-[24%] flex flex-col gap-4 max-xl:flex-row max-xl:w-full max-md:flex-col">
            <div className="h-full flex flex-col bg-white rounded-lg p-4 py-6 max-xl:w-full">
              <h2 className="font-bold text-lg">Orders Status</h2>
              <div className="h-[150px] w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={orderStatusData}
                      cx="50%"
                      cy="50%"
                      innerRadius={50}
                      outerRadius={70}
                      nameKey="name"
                      dataKey="value"
                    >
                      {orderStatusData.map((entry, index) => (
                        <Cell
                          key={`cell-${index}`}
                          fill={COLORS[index % COLORS.length]}
                        />
                      ))}
                    </Pie>
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <div className="flex justify-between items-center w-full">
                {orderStatusData.map((status, index) => {
                  const percent = ((status.value / totalValue) * 100).toFixed(
                    0
                  );

                  return (
                    <div
                      key={index}
                      className="w-full flex flex-col text-center"
                    >
                      <span
                        className="font-bold"
                        style={{ color: COLORS[index % COLORS.length] }}
                      >
                        {percent}%
                      </span>
                      <span className="text-sm capitalize">{status.name}</span>
                    </div>
                  );
                })}
              </div>
            </div>
            {isErrorShopOrderStatistics ? (
              <div className="flex flex-col justify-center text-center items-center bg-white p-4 rounded-lg">
                <ReportProblemIcon fontSize="large" color="error" />
                <span className="font-bold text-xl">
                  Cannot load shop order statistics data
                </span>
              </div>
            ) : isShopOrderStatisticsLoading ? (
              <div className="flex items-center justify-center w-full h-full z-50 bg-white opacity-80">
                <LoadingAnimation />
              </div>
            ) : (
              <div className="flex flex-col bg-white rounded-lg p-4 py-6 max-xl:w-full">
                <div className="flex flex-col gap-2">
                  <div className="flex justify-between items-center">
                    <h2 className="font-bold text-lg">Earning</h2>
                    <span>{format(selectedSalesRatioMonth, "LLLL yyyy")}</span>
                  </div>
                  <div className="flex flex-col gap-2">
                    <div>
                      <span className="text-gray-500">This Month</span>
                      <div className="flex items-center gap-2">
                        <span className="text-lg font-bold">
                          $ {salesRatioThisMonth}
                        </span>
                        <div className="flex items-center">
                          {percentOfSalesRatio > 0 ? (
                            <TrendingUpIcon fontSize="small" color="success" />
                          ) : percentOfSalesRatio < 0 ? (
                            <TrendingDownIcon fontSize="small" color="error" />
                          ) : (
                            <TrendingFlatIcon fontSize="small" color="info" />
                          )}
                          <span
                            className={
                              percentOfSalesRatio > 0
                                ? "text-green-600"
                                : percentOfSalesRatio < 0
                                ? "text-red-600"
                                : "text-gray-600"
                            }
                          >
                            {percentOfSalesRatio?.toFixed(2)}%
                          </span>
                        </div>
                      </div>
                    </div>
                    <div>
                      <span className="text-gray-500">Last Month</span>
                      <div>
                        <span className="text-lg font-bold">
                          ${salesRatioLastMonth}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
          {isErrorShopOrderStatistics ? (
            <div className="flex flex-col justify-center text-center items-center bg-white p-4 rounded-lg w-full mx-4">
              <ReportProblemIcon fontSize="large" color="error" />
              <span className="font-bold text-xl">
                Cannot load shop order statistics data
              </span>
            </div>
          ) : isShopOrderStatisticsLoading ? (
            <div className="flex items-center justify-center w-full h-full z-50 bg-white opacity-80">
              <LoadingAnimation />
            </div>
          ) : (
            <div className="w-[49%] flex flex-col gap-2 bg-white rounded-lg p-4 py-6 max-xl:w-full">
              <div className="flex justify-between items-center">
                <h2 className="font-bold text-lg">Sales Ratio</h2>
                <div className="flex flex-col">
                  <select
                    value={selectedSalesRatioMonth}
                    onChange={(e) => setSelectedSalesRatioMonth(e.target.value)}
                    className="bg-white text-gray-500 text-lg"
                  >
                    {last6Months.map((month, id) => (
                      <option key={id} value={format(month, "yyyy-MM")}>
                        {format(month, "LLLL yyyy")}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="flex items-center gap-8">
                <div className="flex flex-col">
                  <span className="text-gray-500">This month</span>
                  <div className="flex gap-2 items-end">
                    <span className="text-3xl font-semibold text-blue-300">
                      {salesRatioThisMonth}$
                    </span>
                    <span
                      className={
                        percentOfSalesRatio > 0
                          ? "text-green-600"
                          : percentOfSalesRatio < 0
                          ? "text-red-600"
                          : "text-gray-600"
                      }
                    >
                      {percentOfSalesRatio?.toFixed(2)}%
                    </span>
                  </div>
                </div>
                <div className="flex flex-col">
                  <span className="text-gray-400">Last month</span>
                  <span className="text-2xl font-semibold text-gray-400">
                    {salesRatioLastMonth}$
                  </span>
                </div>
              </div>
              <div className="w-full h-[350px]">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart
                    data={salesRatioData}
                    margin={{ top: 5, right: 30, left: 5, bottom: 5 }}
                  >
                    <XAxis dataKey="name" />
                    <YAxis />
                    <CartesianGrid strokeDasharray="3 3" />
                    <Tooltip />
                    <Legend verticalAlign="top" height={36} />
                    <Line
                      name="Current Month Sales"
                      type="monotone"
                      dataKey="cms"
                      stroke="#93c5fd"
                    />
                    <Line
                      name="Last Month Sales"
                      type="monotone"
                      dataKey="lms"
                      stroke="#9ca3af"
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}
          {isErrorStatistics ? (
            <div className="flex flex-col items-center bg-white p-4 rounded-lg justify-center text-center">
              <ReportProblemIcon fontSize="large" color="error" />
              <span className="font-bold text-xl">
                Cannot load statistics data
              </span>
            </div>
          ) : isStatisticsLoading ? (
            <div className="flex items-center justify-center w-full h-full z-50 bg-white opacity-80">
              <LoadingAnimation />
            </div>
          ) : (
            <div className="w-[24%] flex flex-col bg-white rounded-lg p-4 py-6 gap-2 max-xl:w-full">
              <div className="flex justify-between items-center">
                <h2 className="font-bold text-lg">Users</h2>
                <span className="flex flex-col">
                  {format(now, "LLLL yyyy")}
                </span>
              </div>
              <div className="w-full h-full max-xl:h-[200px]">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart
                    data={userData}
                    margin={{ top: 10, right: 0, left: -20, bottom: 0 }}
                  >
                    <defs>
                      <linearGradient id="colorUv" x1="0" y1="0" x2="0" y2="1">
                        <stop
                          offset="5%"
                          stopColor="#8884d8"
                          stopOpacity={0.8}
                        />
                        <stop
                          offset="95%"
                          stopColor="#8884d8"
                          stopOpacity={0}
                        />
                      </linearGradient>
                    </defs>
                    <XAxis dataKey="name" />
                    <YAxis />
                    <CartesianGrid strokeDasharray="3 3" />
                    <Tooltip />
                    <Area
                      type="monotone"
                      dataKey="uv"
                      stroke="#8884d8"
                      fillOpacity={1}
                      fill="url(#colorUv)"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
              <div className="flex flex-col">
                <h2 className="font-bold text-lg">User</h2>
                <div className="flex gap-2 items-end">
                  <h2 className="font-bold text-3xl text-gray-400">
                    {statistics?.usersStatistics?.totalUsers}
                  </h2>
                  <span className="text-green-600">
                    +{percentOfUsersThisMonth}%
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>
        <div className="flex gap-4 max-xl:flex-col">
          {isErrorLatestProductsSales ? (
            <div className="flex flex-col justify-center text-center items-center bg-white p-4 rounded-lg">
              <ReportProblemIcon fontSize="large" color="error" />
              <span className="font-bold text-xl">
                Cannot load latest products sales data
              </span>
            </div>
          ) : isLatestProductsSalesLoading ? (
            <div className="flex items-center justify-center w-full h-full z-50 bg-white opacity-80">
              <LoadingAnimation />
            </div>
          ) : (
            <div className="w-full flex flex-col bg-white rounded-lg p-4 py-6 gap-6">
              <div className="flex justify-between items-center">
                <h2 className="font-bold text-lg">Latest Sales</h2>
              </div>
              <div className="flex justify-between items-center bg-blue-50 p-2">
                <div className="flex flex-col">
                  <span className="text-xl font-semibold">
                    {format(now, "LLLL yyyy")}
                  </span>
                  <span className="text-gray-400">Sales Report</span>
                </div>
                <h2 className="font-bold text-3xl text-blue-400">
                  ${latestSalesProducts}
                </h2>
              </div>
              <table className="min-w-full text-sm text-left">
                <thead className="bg-gray-50 text-xs uppercase border-b-2">
                  <tr>
                    <th className="px-4 py-2 border-b">Name</th>
                    <th className="px-4 py-2 border-b">Order Status</th>
                    <th className="px-4 py-2 border-b">Date</th>
                    <th className="px-4 py-2 border-b">Price</th>
                  </tr>
                </thead>
                <tbody>
                  {latestProductsSales?.map((item, index) => (
                    <tr
                      className="border-b-[1px] border-gray-100 items-center hover:bg-gray-100 cursor-pointer max-md:text-xs"
                      key={index}
                      onClick={() =>
                        navigate(`/profile/adminPanel/orders/${item.orderId}`)
                      }
                    >
                      <td className="px-4 py-2">{item.productName}</td>
                      <td className="px-4 py-2 align-middle">
                        <span
                          className={`rounded-xl px-2 py-1 flex items-center justify-center text-white font-bold w-fit max-md:px-[2px] ${
                            item?.orderStatus === "shipped"
                              ? "bg-purple-300"
                              : item?.orderStatus === "delivered"
                              ? "bg-green-500"
                              : item?.orderStatus === "packing"
                              ? "bg-blue-300"
                              : "bg-gray-400"
                          }`}
                        >
                          {item.orderStatus}
                        </span>
                      </td>
                      <td className="px-4 py-2 capitalize">
                        {format(item.orderCreatedDate, "dd MMM, yyyy")}
                      </td>
                      <td className="px-4 py-2">${item.price}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {isErrorTopProductSales ? (
            <div className="flex flex-col justify-center text-center items-center bg-white p-4 rounded-lg">
              <ReportProblemIcon fontSize="large" color="error" />
              <span className="font-bold text-xl">
                Cannot load top products sales data
              </span>
            </div>
          ) : isTopProductSalesLoading ? (
            <div className="flex items-center justify-center w-full h-full z-50 bg-white opacity-80">
              <LoadingAnimation />
            </div>
          ) : (
            <div className="w-full flex flex-col bg-white rounded-lg p-4 py-6 gap-6">
              <div className="flex justify-between items-center">
                <h2 className="font-bold text-lg">Top Products Sales</h2>
                <select
                  value={selectedTopProductsSalesMonth}
                  onChange={(e) =>
                    setSelectedTopProductsSalesMonth(e.target.value)
                  }
                  className="bg-white text-gray-500 text-lg"
                >
                  {last6Months.map((month, id) => (
                    <option key={id} value={format(month, "yyyy-MM")}>
                      {format(month, "LLLL yyyy")}
                    </option>
                  ))}
                </select>
              </div>
              <div className="w-full min-h-[300px] max-xl:h-[300px]">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={topProductSalesData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis
                      dataKey="name"
                      className="truncate"
                      tickFormatter={(name) =>
                        name.length > 25 ? name.substring(0, 25) + "..." : name
                      }
                    />
                    <YAxis />
                    <Tooltip />
                    {/* <Legend /> */}
                    <Bar dataKey="totalQuantity">
                      {topProductSalesData?.map((entry, index) => (
                        <Cell
                          key={`cell-${index}`}
                          fill={COLORS[index % COLORS.length]}
                        />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
              <div className="flex justify-around items-center mt-4">
                {topProductSalesData?.map((product, index) => (
                  <div
                    key={index}
                    className="flex flex-col items-center mx-2 cursor-pointer"
                    onClick={() =>
                      navigate(
                        `/products/${product.productId}-${product.colour}`
                      )
                    }
                  >
                    <span
                      className="px-2 font-bold text-sm text-white rounded"
                      style={{ backgroundColor: COLORS[index % COLORS.length] }}
                    >
                      {(
                        (product.totalQuantity / topProductsSalesQuantitySum) *
                        100
                      ).toFixed(0)}
                      %
                    </span>
                    <span className="text-gray-400 text-sm text-center">
                      {product.name}
                    </span>
                    <div className="flex items-end">
                      <span className="text-lg font-bold">
                        {product.totalQuantity}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProfileAdminStatistics;
