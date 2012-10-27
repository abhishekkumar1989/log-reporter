package pw.server.logreporter.util;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
public class NullChecker
{
	public static boolean isEmpty(final String str)
	{
		return (str == null) || (str.trim().length() == 0) || str.trim().equalsIgnoreCase("null");
	}

	public static boolean isNull(final Object obj)
	{
		return obj == null;
	}

	public static boolean isNotNull(final Object obj)
	{
		return !isNull(obj);
	}

	public static boolean isNull(final Object... objs)
	{
		if (objs == null)
		{
			return true;
		}

		boolean isNull = true;
		for (Object obj : objs)
		{
			isNull &= isNull(obj);
			if (!isNull)
			{
				break;
			}
		}

		return isNull;
	}

	public static boolean isNullOrEmpty(final Collection<?> collection)
	{
		return isNull(collection) || collection.isEmpty();
	}

	public static boolean isNullOrEmpty(final Map<?, ?> map)
	{
		return isNull(map) || map.isEmpty();
	}

	public static boolean isAnyValueNullOrEmpty(final String... strs)
	{
		if (strs == null)
		{
			return true;
		}

		for (String str : strs)
		{
			if (isEmpty(str))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isAnyValueNullOrEmpty(final Collection<String> collection)
	{
		return isNullOrEmpty(collection) || isAnyValueNullOrEmpty(collection.toArray(new String[]{}));
	}

	public static boolean isAnyValueNullOrEmpty(final Map<String, String> map)
	{
		return isNullOrEmpty(map) || isAnyValueNullOrEmpty(map.values());
	}

    public static boolean isNotEmpty(final String str) {
        return !isEmpty(str);
    }

}
