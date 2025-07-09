import { useState } from 'react';
import { useFormContext } from 'react-hook-form';
import { FiEye, FiEyeOff } from 'react-icons/fi';

export default function LabelInputLogin({ label, name, type, validationRules, ...rest }) {
  const {
    register,
    formState: { errors, isSubmitting },
  } = useFormContext();

  const [showPassword, setShowPassword] = useState(false);
  const isPassword = type === 'password';
  const inputType = isPassword && showPassword ? 'text' : type;

  return (
    <div className="relative">
      <label htmlFor={name} className="block text-gray-700">{label}</label>
      <input
        {...register(name, validationRules)}
        id={name}
        type={inputType}
        disabled={isSubmitting}
        className={`w-full p-2 border ${errors[name] ? 'border-red-500' : 'border-gray-300'} 
          rounded mt-1 focus:outline-none focus:ring-2 focus:ring-red-400`}
        {...rest}
      />
      {isPassword && (
        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="absolute right-3 top-10 text-gray-500 hover:text-gray-700"
          tabIndex={-1}
        >
          {showPassword ? <FiEyeOff /> : <FiEye />}
        </button>
      )}
      {errors[name] && (
        <p className="text-sm text-red-500 mt-1" data-cy="label_input_error">
          {errors[name].message}
        </p>
      )}
    </div>
  );
}
