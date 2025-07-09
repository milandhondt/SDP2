const Information = ({ icon: Icon, info }) => {
  return (
    <div className="border px-4 py-2 rounded-md mb-5">
      <div className="flex items-center">
        <Icon className="text-2xl w-12"/>
        <p className="ml-2">{info}</p>
      </div>
    </div>
  );
};

export default Information;