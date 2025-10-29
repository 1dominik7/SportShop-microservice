import React, { useState } from "react";
import LoadingAnimation from "../../../ui/LoadingAnimation";
import { useAllUsers } from "../../../hooks/query";
import { Pagination } from "@mui/material";
import { useNavigate } from "react-router";

const ProfileAdminUsers = () => {
  const navigate = useNavigate();

  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(10);
  const [sortBy, setSortBy] = useState<string>("id");
  const [direction, setDirection] = useState<string>("desc");
  const [searchBy, setSearchBy] = useState<string>("userId");
  const [query, setQuery] = useState<string>("");
  const [inputValue, setInputValue] = useState<string>("");

  const { data, isLoading } = useAllUsers(
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

  return (
    <div className="w-full h-full relative flex flex-col gap-4 p-6">
      {isLoading ? (
        <div className="flex items-center justify-center w-full h-full z-50 bg-white opacity-80">
          <LoadingAnimation />
        </div>
      ) : (
        <div className="flex flex-col gap-2">
          <div className="flex justify-between items-center">
            <div className="flex justify-between items-center gap-4">
              <div className="flex gap-2 items-center max-md:flex-col">
                <span className="font-bold">Select By:</span>
                <select
                  name=""
                  id=""
                  className="p-2 cursor-pointer"
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                >
                  <option value="id">Id</option>
                  <option value="firstname">First Name</option>
                  <option value="lastname">Last Name</option>
                  <option value="email">Email</option>
                  <option value="createdDate">Created Date</option>
                  <option value="roles">Roles</option>
                </select>
              </div>
              <div className="flex gap-2 items-center max-md:flex-col">
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
              <div className="flex gap-2 items-center max-md:flex-col">
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
          <div
            className="flex gap-2 items-center pb-2 max-md:flex-col max-md:items-start
          "
          >
            <span className="font-bold">Search By:</span>
            <select
              name=""
              id=""
              className="p-2 cursor-pointer"
              value={searchBy}
              onChange={(e) => setSearchBy(e.target.value)}
            >
              <option value="userId">userId</option>
              <option value="email">email</option>
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
                <th className="px-4 py-2 border-b">ID</th>
                <th className="px-4 py-2 border-b max-xl:hidden">First Name</th>
                <th className="px-4 py-2 border-b max-xl:hidden">Last Name</th>
                <th className="px-4 py-2 border-b">Email</th>
                <th className="px-4 py-2 border-b max-2xl:hidden">
                  Created Date
                </th>
                <th className="px-4 py-2 border-b max-md:hidden">Roles</th>
              </tr>
            </thead>
            <tbody>
              {data?.content?.map((user) => (
                <tr
                  key={user.id}
                  className="border-b hover:bg-gray-50 cursor-pointer"
                  onClick={() => navigate(`${user?.id}`)}
                >
                  <td className="px-4 py-2 max-md:text-xs max-md:truncate max-md:max-w-[120px]">
                    {user?.id}
                  </td>
                  <td className="px-4 py-2 max-xl:hidden">{user?.firstname}</td>
                  <td className="px-4 py-2 capitalize max-xl:hidden">
                    {user?.lastname}
                  </td>
                  <td className="px-4 py-2 max-md:text-xs max-md:truncate max-md:w-[120px]">
                    {user?.email}
                  </td>
                  <td className="px-4 py-2 max-2xl:hidden max-md:truncate max-md:w-[120px]">
                    {user?.createdDate ? formatDate(user.createdDate) : "-"}
                  </td>
                  <td className="px-4 py-2 max-md:hidden">
                    {user?.roleNames?.join(", ")}
                  </td>
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

export default ProfileAdminUsers;
