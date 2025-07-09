const GenericButton = ({ onClick, text, icon: Icon, otherProps, dataCy }) => {
  return (
    <button 
      className="bg-red-500 hover:cursor-pointer hover:bg-red-700 transition-all duration-300
          text-white font-bold py-2 px-4 
          rounded flex items-center gap-x-2 mb-6 mt-10"
      onClick={onClick}
      {...otherProps}
    >
      {Icon && <Icon />}
      <span data-cy={dataCy}>{text}</span>
    </button>
  );
};

export default GenericButton;