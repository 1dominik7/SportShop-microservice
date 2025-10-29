import React, { useEffect, useState } from "react";
import LoadingAnimation from "../../../ui/LoadingAnimation";
import { useAllShopOrders } from "../../../hooks/query";
import { Pagination } from "@mui/material";
import { OrderStatus } from "../../../types/userTypes";
import { api } from "../../../config/api";
import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router";

const ProfileAdminOrders = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(10);
  const [sortBy, setSortBy] = useState<string>("id");
  const [direction, setDirection] = useState<string>("desc");
  const [orderStatus, setOrderStatus] = useState<OrderStatus[]>([]);
  const [selectedOrderStatusName, setSelectedOrderStatusName] =
    useState<string>("");
  const [selectedOrderStatus, setSelectedOrderStatus] = useState<number[]>([]);
  const [searchBy, setSearchBy] = useState<string>("orderId");
  const [inputValue, setInputValue] = useState<string>("");
  const [query, setQuery] = useState<string>("");

  const { data, isLoading } = useAllShopOrders(
    page,
    size,
    sortBy,
    direction,
    query,
    searchBy
  );

  const onChangeHandler = (
    event: React.ChangeEvent<unknown>,
    value: number
  ) => {
    setPage(value - 1);
  };

  const formatDate = (timestamp: string) => {
    const date = new Date(timestamp);
    return new Intl.DateTimeFormat("pl-PL", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }).format(date);
  };

  const changeOrderStatusHandler = async () => {
    try {
      await api.put(
        `/api/v1/shop-order/order-status/${selectedOrderStatusName}?shopOrderIds=${selectedOrderStatus}`
      );
      queryClient.invalidateQueries({
        queryKey: ["allShopOrders"],
      });
      setSelectedOrderStatus([]);
      setSelectedOrderStatusName("");
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    const getOrderStatus = async () => {
      try {
        const res = await api.get(`/api/v1/order-status/all`);
        setOrderStatus(res.data);
      } catch (error) {
        console.error(error);
      }
    };
    getOrderStatus();
  }, []);

  return (
    <div className="w-full h-full relative flex flex-col gap-4 p-6 max-md:p-3">
      {isLoading ? (
        <div className="flex items-center justify-center w-full h-full z-50 bg-white opacity-80">
          <LoadingAnimation />
        </div>
      ) : (
        <div className="flex flex-col gap-2">
          <div className="flex justify-between items-center max-xl:flex-col">
            <div className="flex py-4 gap-4 items-center max-md:flex-col max-md:w-full max-md:items-start">
              <span className="font-bold">Change Order Status</span>
              <select
                name=""
                id=""
                className="flex p-2 cursor-pointer capitalize"
              >
                <option value="">Select Order Status</option>
                {orderStatus?.map((os) => (
                  <option
                    value={os.status}
                    key={os.id}
                    className="p-2 flex capitalize"
                    onClick={() => setSelectedOrderStatusName(os.status)}
                  >
                    {os.status}
                  </option>
                ))}
              </select>
              <button
                className="h-[40px] ml-2 px-6 bg-gray-200 rounded-xl border-[1px] border-black hover:opacity-80 font-bold"
                onClick={changeOrderStatusHandler}
              >
                Change
              </button>
            </div>
            <div className="flex justify-between items-center gap-4 max-md:flex-col max-md:items-start max-md:w-full">
              <div className="flex gap-2 items-center">
                <span className="font-bold">Select By:</span>
                <select
                  name=""
                  id=""
                  className="p-2 cursor-pointer"
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                >
                  <option value="id">Id</option>
                  <option value="orderDate">Order Date</option>
                  <option value="paymentCreatedAt">Payment Date</option>
                  <option value="paymentStatus">Payment Status</option>
                  <option value="userId">User Id</option>
                </select>
              </div>
              <div className="flex gap-2 items-center">
                <span className="font-bold">Direction:</span>
                <select
                  name=""
                  id=""
                  className="p-2 cursor-pointer"
                  onChange={(e) => setDirection(e.target.value)}
                >
                  <option value="asc">Asc</option>
                  <option value="desc">Desc</option>
                </select>
              </div>
              <div className="flex gap-2 items-center">
                <span className="font-bold">Size:</span>
                <select
                  name=""
                  id=""
                  className="p-2 cursor-pointer"
                  onChange={(e) => setSize(Number(e.target.value))}
                >
                  <option value={10}>10</option>
                  <option value={25}>25</option>
                  <option value={50}>50</option>
                </select>
              </div>
            </div>
          </div>
          <div className="flex gap-2 items-center pb-2 max-md:flex-col max-md:w-full max-md:items-start">
            <span className="font-bold">Search By:</span>
            <select
              name=""
              id=""
              className="p-2 cursor-pointer"
              value={searchBy}
              onChange={(e) => setSearchBy(e.target.value)}
            >
              <option value="orderId">orderId</option>
              <option value="userId">userId</option>
            </select>
            <input
              type="text"
              name=""
              id=""
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              className="border-2 border-black rounded-md p-[3px]"
            />
            <button
              className="h-[40px] ml-2 px-6 bg-gray-200 rounded-xl border-[1px] border-black hover:opacity-80 font-bold"
              onClick={() => {
                setQuery(inputValue);
                setPage(0);
              }}
            >
              Search
            </button>
          </div>
          <table className="min-w-full text-sm text-left border border-gray-300">
            <thead className="bg-gray-100 text-xs uppercase">
              <tr>
                <th className="px-4 py-2 border-b max-md:px-2">SELECT</th>
                <th className="px-4 py-2 border-b max-md:px-2">ID</th>
                <th className="px-4 py-2 border-b max-md:px-2 max-sm:hidden">Order Date</th>
                <th className="px-4 py-2 border-b max-xl:hidden">Order Status</th>
                <th className="px-4 py-2 border-b max-xl:hidden" >Payment Status</th>
                <th className="px-4 py-2 border-b max-md:hidden">Payment Date</th>
                <th className="px-4 py-2 border-b max-md:px-2">USER ID</th>
              </tr>
            </thead>
            <tbody>
              {data?.content?.map((order) => (
                <tr
                  key={order.id}
                  className="border-b hover:bg-gray-50 cursor-pointer max-md:text-xs"
                  onClick={() => navigate(`${order.id}`)}
                >
                  <td className="px-4 py-2 flex">
                    <input
                      type="checkbox"
                      checked={selectedOrderStatus.includes(order.id)}
                      onClick={(e) => e.stopPropagation()}
                      onChange={() => {
                        setSelectedOrderStatus((prev) =>
                          prev.includes(order.id)
                            ? prev.filter((id) => id !== order.id)
                            : [...prev, order.id]
                        );
                      }}
                    ></input>
                  </td>
                  <td className="px-4 py-2">{order?.id}</td>
                  <td className="px-4 py-2 max-sm:hidden">
                    {order?.orderDate ? formatDate(order.orderDate) : "-"}
                  </td>
                  <td className="px-4 py-2 capitalize max-xl:hidden">
                    {order?.orderStatus?.status || "-"}
                  </td>
                  <td className="px-4 py-2 max-xl:hidden">{order?.paymentStatus || "-"}</td>
                  <td className="px-4 py-2 max-md:hidden">
                    {order?.paymentCreatedAt
                      ? formatDate(order.paymentCreatedAt)
                      : "-"}
                  </td>
                  <td className="px-4 py-2 max-md:truncate">{order?.userId}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="flex justify-center pt-10">
            <Pagination
              count={data?.totalPages}
              page={page + 1}
              defaultPage={1}
              siblingCount={0}
              boundaryCount={2}
              shape="rounded"
              onChange={onChangeHandler}
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default ProfileAdminOrders;
