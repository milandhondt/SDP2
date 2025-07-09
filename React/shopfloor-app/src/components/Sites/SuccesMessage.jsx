export default function SuccessMessage({ message }) {
  return (
    <div 
      className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4" 
      data-cy="success-message"
    >
      {message}
    </div>
  );
}